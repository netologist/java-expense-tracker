package com.hozgan.expensetracker;

import com.hozgan.expensetracker.application.expense.AddExpenseUseCase;
import com.hozgan.expensetracker.application.expense.ListExpensesUseCase;
import com.hozgan.expensetracker.application.expense.RemoveExpenseUseCase;
import com.hozgan.expensetracker.application.sync.PullExpensesUseCase;
import com.hozgan.expensetracker.application.sync.PushExpensesUseCase;
import com.hozgan.expensetracker.cli.AddExpenseCliCommand;
import com.hozgan.expensetracker.cli.ExpenseTrackerCommand;
import com.hozgan.expensetracker.cli.ListExpensesCliCommand;
import com.hozgan.expensetracker.cli.RemoveExpenseCliCommand;
import com.hozgan.expensetracker.cli.sync.PullExpensesCliCommand;
import com.hozgan.expensetracker.cli.sync.PushExpensesCliCommand;
import com.hozgan.expensetracker.cli.sync.SyncCliCommand;
import com.hozgan.expensetracker.infrastructure.persistence.JsonExpenseRepository;
import com.hozgan.expensetracker.infrastructure.sync.S3ExpenseSyncAdapter;
import com.hozgan.expensetracker.infrastructure.sync.S3ExpenseSyncConfig;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Path;
import picocli.CommandLine;

public final class ExpenseTrackerApplication {

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    String dataFilePath = System.getProperty("expense.tracker.data.file");
    if (dataFilePath == null || dataFilePath.isBlank()) {
      dataFilePath = dotenv.get("EXPENSE_TRACKER_DATA_FILE", "expenses.json");
    }
    Path localFilePath = Path.of(dataFilePath);

    var expenseRepository = new JsonExpenseRepository(localFilePath);
    var addExpenseUseCase = new AddExpenseUseCase(expenseRepository);
    var listExpensesUseCase = new ListExpensesUseCase(expenseRepository);
    var removeExpenseUseCase = new RemoveExpenseUseCase(expenseRepository);

    var syncConfig = S3ExpenseSyncConfig.fromEnvironment(dotenv);
    var syncPort = new S3ExpenseSyncAdapter(syncConfig);
    var pushExpensesUseCase = new PushExpensesUseCase(syncPort, localFilePath);
    var pullExpensesUseCase = new PullExpensesUseCase(syncPort, localFilePath);

    var rootCommand = new ExpenseTrackerCommand();
    var commandLine = new CommandLine(rootCommand);

    commandLine.addSubcommand("add", new AddExpenseCliCommand(addExpenseUseCase));
    commandLine.addSubcommand("list", new ListExpensesCliCommand(listExpensesUseCase));
    commandLine.addSubcommand("remove", new RemoveExpenseCliCommand(removeExpenseUseCase));

    var syncCommand = new CommandLine(new SyncCliCommand());
    syncCommand.addSubcommand("push", new PushExpensesCliCommand(pushExpensesUseCase));
    syncCommand.addSubcommand("pull", new PullExpensesCliCommand(pullExpensesUseCase));
    commandLine.addSubcommand("sync", syncCommand);

    int exitCode = commandLine.execute(args);

    System.exit(exitCode);
  }
}
