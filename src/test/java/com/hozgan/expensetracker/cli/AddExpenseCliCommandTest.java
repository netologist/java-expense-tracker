package com.hozgan.expensetracker.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.hozgan.expensetracker.application.expense.AddExpenseUseCase;
import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpenseCategory;
import com.hozgan.expensetracker.domain.expense.ExpenseRepository;
import com.hozgan.expensetracker.infrastructure.persistence.InMemoryExpenseRepository;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import picocli.CommandLine;

class AddExpenseCliCommandTest {

  private final StringWriter stdout = new StringWriter();
  private final StringWriter stderr = new StringWriter();
  private final ExpenseRepository expenseRepository = spy(new InMemoryExpenseRepository());
  private final AddExpenseUseCase useCase = new AddExpenseUseCase(expenseRepository);

  private int execute(String... args) {
    var commandLine = new CommandLine(new AddExpenseCliCommand(useCase));
    commandLine.setOut(new PrintWriter(stdout, true));
    commandLine.setErr(new PrintWriter(stderr, true));
    return commandLine.execute(args);
  }

  private Expense savedExpense() {
    var captor = ArgumentCaptor.forClass(Expense.class);
    verify(expenseRepository).save(captor.capture());
    return captor.getValue();
  }

  @Test
  void shouldAddExpenseWithShortOptionsAndDefaultDate() {
    int exitCode = execute("-a", "12.50", "-c", "GBP", "--category", "FOOD", "-d", "Lunch");

    assertThat(exitCode).isZero();

    var expense = savedExpense();
    assertThat(expense.id()).isNotNull();
    assertThat(expense.amount()).isEqualTo(Money.of(new BigDecimal("12.50"), "GBP"));
    assertThat(expense.category()).isEqualTo(ExpenseCategory.FOOD);
    assertThat(expense.description()).isEqualTo("Lunch");
    assertThat(expense.date()).isEqualTo(LocalDate.now(ZoneId.systemDefault()));

    assertThat(stdout.toString())
        .contains("Expense added successfully.")
        .contains("ID: " + expense.id())
        .contains("GBP 12.5")
        .contains("Date: " + expense.date());
  }

  @Test
  void shouldAddExpenseWithExplicitDateAndLongOptions() {
    int exitCode =
        execute(
            "--amount",
            "120.00",
            "--currency",
            "USD",
            "--category",
            "SHOPPING",
            "--description",
            "Headphones",
            "--date",
            "2026-09-01");

    assertThat(exitCode).isZero();

    var expense = savedExpense();
    assertThat(expense.amount()).isEqualTo(Money.of(new BigDecimal("120.00"), "USD"));
    assertThat(expense.category()).isEqualTo(ExpenseCategory.SHOPPING);
    assertThat(expense.description()).isEqualTo("Headphones");
    assertThat(expense.date()).isEqualTo(LocalDate.of(2026, 9, 1));
  }

  @Test
  void shouldRejectNegativeAmount() {
    int exitCode = execute("-a", "-10", "-c", "GBP", "--category", "FOOD", "-d", "Invalid");

    assertThat(exitCode).isEqualTo(1);
    assertThat(stderr.toString()).contains("Error: amount must be greater than zero");
    verify(expenseRepository, never()).save(any());
  }

  @Test
  void shouldRejectZeroAmount() {
    int exitCode = execute("-a", "0", "-c", "GBP", "--category", "FOOD", "-d", "Invalid");

    assertThat(exitCode).isEqualTo(1);
    assertThat(stderr.toString()).contains("Error: amount must be greater than zero");
  }

  @Test
  void shouldRejectInvalidCurrency() {
    int exitCode = execute("-a", "10", "-c", "INVALID", "--category", "FOOD", "-d", "Invalid");

    assertThat(exitCode).isEqualTo(1);
    assertThat(stderr.toString()).contains("Error: invalid currency code: INVALID");
    verify(expenseRepository, never()).save(any());
  }

  @Test
  void shouldRejectBlankDescription() {
    int exitCode = execute("-a", "10", "-c", "GBP", "--category", "FOOD", "-d", "   ");

    assertThat(exitCode).isEqualTo(1);
    assertThat(stderr.toString()).contains("Error: description must not be blank");
    verify(expenseRepository, never()).save(any());
  }

  @Test
  void shouldRejectInvalidCategory() {
    int exitCode = execute("-a", "10", "-c", "GBP", "--category", "NOPE", "-d", "Invalid");

    assertThat(exitCode).isEqualTo(2);
    assertThat(stderr.toString()).contains("Invalid value for option '--category'");
    verify(expenseRepository, never()).save(any());
  }

  @Test
  void shouldRejectInvalidDateFormat() {
    int exitCode =
        execute(
            "-a", "10", "-c", "GBP", "--category", "FOOD", "-d", "Invalid", "--date", "07/09/2026");

    assertThat(exitCode).isEqualTo(2);
    assertThat(stderr.toString()).contains("Invalid value for option '--date'");
    verify(expenseRepository, never()).save(any());
  }

  @Test
  void shouldRejectMissingRequiredOption() {
    int exitCode = execute("-a", "10", "-c", "GBP", "--category", "FOOD");

    assertThat(exitCode).isEqualTo(2);
    assertThat(stderr.toString()).contains("Missing required option");
    verify(expenseRepository, never()).save(any());
  }

  @Test
  void shouldRejectNonNumericAmount() {
    int exitCode = execute("-a", "abc", "-c", "GBP", "--category", "FOOD", "-d", "Invalid");

    assertThat(exitCode).isEqualTo(2);
    assertThat(stderr.toString()).contains("Invalid value for option '--amount'");
    verify(expenseRepository, never()).save(any());
  }

  @Test
  void shouldDisplayHelpAndExitSuccessfully() {
    int exitCode = execute("--help");

    assertThat(exitCode).isZero();
    assertThat(stdout.toString()).contains("Usage: add");
    verify(expenseRepository, never()).save(any());
  }
}
