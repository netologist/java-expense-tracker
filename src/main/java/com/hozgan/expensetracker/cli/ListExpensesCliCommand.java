package com.hozgan.expensetracker.cli;

import com.hozgan.expensetracker.application.expense.ListExpensesUseCase;
import com.hozgan.expensetracker.domain.expense.Expense;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "list", mixinStandardHelpOptions = true, description = "List all expenses.")
@RequiredArgsConstructor
public final class ListExpensesCliCommand implements Callable<Integer> {

  private final ListExpensesUseCase listExpensesUseCase;

  @Spec private CommandSpec spec;

  @Override
  public Integer call() {
    List<Expense> expenses = listExpensesUseCase.execute();
    var out = spec.commandLine().getOut();

    if (expenses.isEmpty()) {
      out.println("No expenses found.");
      return 0;
    }

    out.printf(
        "%-36s  %-10s  %-13s  %-12s  %s%n", "ID", "DATE", "CATEGORY", "AMOUNT", "DESCRIPTION");
    out.println("-".repeat(89));

    for (Expense expense : expenses) {
      String formattedAmount = formatAmount(expense);
      out.printf(
          "%-36s  %-10s  %-13s  %-12s  %s%n",
          expense.id(), expense.date(), expense.category(), formattedAmount, expense.description());
    }

    return 0;
  }

  private String formatAmount(Expense expense) {
    BigDecimal amount =
        expense
            .amount()
            .getNumber()
            .numberValue(BigDecimal.class)
            .setScale(2, RoundingMode.HALF_EVEN);
    String currency = expense.amount().getCurrency().getCurrencyCode();
    return currency + " " + amount.toPlainString();
  }
}
