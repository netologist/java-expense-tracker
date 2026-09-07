package com.hozgan.expensetracker;

import com.hozgan.expensetracker.application.expense.AddExpenseUseCase;
import com.hozgan.expensetracker.cli.AddExpenseCliCommand;
import com.hozgan.expensetracker.cli.ExpenseTrackerCommand;
import com.hozgan.expensetracker.infrastructure.persistence.InMemoryExpenseRepository;
import picocli.CommandLine;

public final class ExpenseTrackerApplication {
  public static void main(String[] args) {
    var expenseRepository = new InMemoryExpenseRepository();
    var addExpenseUseCase = new AddExpenseUseCase(expenseRepository);

    var rootCommand = new ExpenseTrackerCommand();
    var commandLine = new CommandLine(rootCommand);

    commandLine.addSubcommand("add", new AddExpenseCliCommand(addExpenseUseCase));

    int exitCode = commandLine.execute(args);

    System.exit(exitCode);
  }
}
