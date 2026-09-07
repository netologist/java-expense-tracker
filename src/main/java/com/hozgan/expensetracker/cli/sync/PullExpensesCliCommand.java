package com.hozgan.expensetracker.cli.sync;

import com.hozgan.expensetracker.application.sync.OverwriteNotAllowedException;
import com.hozgan.expensetracker.application.sync.PullExpensesUseCase;
import com.hozgan.expensetracker.application.sync.SyncException;
import com.hozgan.expensetracker.application.sync.SyncResult;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(
    name = "pull",
    mixinStandardHelpOptions = true,
    description = "Download expenses from S3-compatible storage.")
@RequiredArgsConstructor
public final class PullExpensesCliCommand implements Callable<Integer> {

  private final PullExpensesUseCase pullExpensesUseCase;

  @Spec private CommandSpec spec;

  @Option(
      names = {"-f", "--force"},
      description = "Overwrite local data file if it exists.")
  private boolean force;

  @Override
  public Integer call() {
    try {
      SyncResult result = pullExpensesUseCase.execute(force);
      var out = spec.commandLine().getOut();
      out.printf("Expenses synced successfully.%n%n");
      out.printf("Bucket: %s%n", result.bucket());
      out.printf("Object: %s%n", result.objectKey());
      return 0;
    } catch (OverwriteNotAllowedException e) {
      spec.commandLine().getErr().println(e.getMessage());
      return 1;
    } catch (SyncException e) {
      spec.commandLine().getErr().println("Error: " + e.getMessage());
      return 1;
    }
  }
}
