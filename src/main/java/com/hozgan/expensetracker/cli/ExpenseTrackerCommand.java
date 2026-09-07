package com.hozgan.expensetracker.cli;

import picocli.CommandLine.Command;

@Command(
    name = "expense-tracker",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "A command-line expense tracker.")
public final class ExpenseTrackerCommand implements Runnable {

  @Override
  public void run() {
    System.out.println("Use --help to see available commands.");
  }
}
