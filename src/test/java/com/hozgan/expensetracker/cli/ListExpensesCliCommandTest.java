package com.hozgan.expensetracker.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hozgan.expensetracker.application.expense.ListExpensesUseCase;
import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpenseCategory;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class ListExpensesCliCommandTest {

  private final ListExpensesUseCase useCase = mock(ListExpensesUseCase.class);
  private final StringWriter stdout = new StringWriter();
  private final StringWriter stderr = new StringWriter();

  private int execute(String... args) {
    var commandLine = new CommandLine(new ListExpensesCliCommand(useCase));
    commandLine.setOut(new PrintWriter(stdout, true));
    commandLine.setErr(new PrintWriter(stderr, true));
    return commandLine.execute(args);
  }

  @Test
  void shouldDisplayEmptyMessageWhenNoExpenses() {
    when(useCase.execute()).thenReturn(List.of());

    int exitCode = execute();

    assertThat(exitCode).isZero();
    assertThat(stdout.toString()).contains("No expenses found.");
  }

  @Test
  void shouldDisplayFormattedTableWhenExpensesExist() {
    var id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    var expense =
        new Expense(
            id,
            Money.of(new BigDecimal("12.50"), "GBP"),
            ExpenseCategory.FOOD,
            "Lunch",
            LocalDate.of(2026, 9, 7));
    when(useCase.execute()).thenReturn(List.of(expense));

    int exitCode = execute();

    assertThat(exitCode).isZero();
    String output = stdout.toString();
    assertThat(output)
        .contains("ID")
        .contains("DATE")
        .contains("CATEGORY")
        .contains("AMOUNT")
        .contains("DESCRIPTION")
        .contains("123e4567-e89b-12d3-a456-426614174000")
        .contains("2026-09-07")
        .contains("FOOD")
        .contains("GBP 12.50")
        .contains("Lunch");
  }

  @Test
  void shouldDisplayHelpAndExitSuccessfully() {
    int exitCode = execute("--help");

    assertThat(exitCode).isZero();
    assertThat(stdout.toString()).contains("Usage: list");
  }
}
