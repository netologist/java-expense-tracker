package com.hozgan.expensetracker.cli;

import com.hozgan.expensetracker.application.expense.AddExpenseCommand;
import com.hozgan.expensetracker.application.expense.AddExpenseUseCase;
import com.hozgan.expensetracker.domain.expense.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Currency;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "add", mixinStandardHelpOptions = true, description = "Add a new expense.")
@RequiredArgsConstructor
public final class AddExpenseCliCommand implements Callable<Integer> {

  private final AddExpenseUseCase addExpenseUseCase;

  @Spec private CommandSpec spec;

  @Option(
      names = {"-a", "--amount"},
      required = true,
      description = "Expense amount.")
  private BigDecimal amount;

  @Option(
      names = {"-c", "--currency"},
      required = true,
      description = "Currency code, for example GBP or USD.")
  private String currency;

  @Option(
      names = {"--category"},
      required = true,
      description = "Expense category.")
  private ExpenseCategory category;

  @Option(
      names = {"-d", "--description"},
      required = true,
      description = "Expense description.")
  private String description;

  @Option(
      names = {"--date"},
      description = "Expense date in ISO-8601 format (YYYY-MM-DD). Defaults to today.")
  private LocalDate date;

  @Override
  public Integer call() {
    if (amount.signum() <= 0) {
      return error("amount must be greater than zero");
    }

    Currency expenseCurrency;
    try {
      expenseCurrency = Currency.getInstance(currency);
    } catch (IllegalArgumentException e) {
      return error("invalid currency code: " + currency);
    }

    if (description.isBlank()) {
      return error("description must not be blank");
    }

    var expenseDate = date != null ? date : LocalDate.now(ZoneId.systemDefault());

    var command =
        new AddExpenseCommand(amount, expenseCurrency, category, description, expenseDate);

    var expense = addExpenseUseCase.execute(command);

    var out = spec.commandLine().getOut();
    out.printf("Expense added successfully.%n%n");
    out.printf("ID: %s%n", expense.id());
    out.printf("Amount: %s%n", expense.amount());
    out.printf("Category: %s%n", expense.category());
    out.printf("Description: %s%n", expense.description());
    out.printf("Date: %s%n", expense.date());

    return 0;
  }

  private int error(String message) {
    spec.commandLine().getErr().println("Error: " + message);
    return 1;
  }
}
