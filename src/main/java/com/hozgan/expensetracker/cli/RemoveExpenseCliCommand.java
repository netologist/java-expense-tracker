package com.hozgan.expensetracker.cli;

import com.hozgan.expensetracker.application.expense.ExpenseNotFoundException;
import com.hozgan.expensetracker.application.expense.RemoveExpenseCommand;
import com.hozgan.expensetracker.application.expense.RemoveExpenseUseCase;
import java.util.UUID;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "remove", mixinStandardHelpOptions = true, description = "Remove an expense by ID.")
@RequiredArgsConstructor
public final class RemoveExpenseCliCommand implements Callable<Integer> {

  private final RemoveExpenseUseCase removeExpenseUseCase;

  @Spec private CommandSpec spec;

  @Option(
      names = {"-i", "--id"},
      required = true,
      description = "Expense ID to remove.")
  private UUID id;

  @Override
  public Integer call() {
    try {
      removeExpenseUseCase.execute(new RemoveExpenseCommand(id));
      var out = spec.commandLine().getOut();
      out.printf("Expense removed successfully.%n%n");
      out.printf("ID: %s%n", id);
      return 0;
    } catch (ExpenseNotFoundException e) {
      spec.commandLine().getErr().println("Expense not found: " + id);
      return 1;
    }
  }
}
