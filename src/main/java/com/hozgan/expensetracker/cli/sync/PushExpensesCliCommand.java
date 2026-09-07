package com.hozgan.expensetracker.cli.sync;

import com.hozgan.expensetracker.application.sync.PushExpensesUseCase;
import com.hozgan.expensetracker.application.sync.SyncException;
import com.hozgan.expensetracker.application.sync.SyncResult;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(
    name = "push",
    mixinStandardHelpOptions = true,
    description = "Upload local expenses to S3-compatible storage.")
@RequiredArgsConstructor
public final class PushExpensesCliCommand implements Callable<Integer> {

  private final PushExpensesUseCase pushExpensesUseCase;

  @Spec private CommandSpec spec;

  @Override
  public Integer call() {
    try {
      SyncResult result = pushExpensesUseCase.execute();
      var out = spec.commandLine().getOut();
      out.printf("Expenses synced successfully.%n%n");
      out.printf("Bucket: %s%n", result.bucket());
      out.printf("Object: %s%n", result.objectKey());
      return 0;
    } catch (SyncException e) {
      spec.commandLine().getErr().println("Error: " + e.getMessage());
      return 1;
    }
  }
}
