package com.hozgan.expensetracker.cli.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hozgan.expensetracker.application.sync.PushExpensesUseCase;
import com.hozgan.expensetracker.application.sync.SyncException;
import com.hozgan.expensetracker.application.sync.SyncResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class PushExpensesCliCommandTest {

  private final PushExpensesUseCase useCase = mock(PushExpensesUseCase.class);
  private final StringWriter stdout = new StringWriter();
  private final StringWriter stderr = new StringWriter();

  private int execute(String... args) {
    var commandLine = new CommandLine(new PushExpensesCliCommand(useCase));
    commandLine.setOut(new PrintWriter(stdout, true));
    commandLine.setErr(new PrintWriter(stderr, true));
    return commandLine.execute(args);
  }

  @Test
  void shouldDisplaySuccessMessageOnSuccessfulPush() {
    when(useCase.execute()).thenReturn(new SyncResult("my-bucket", "expenses.json"));

    int exitCode = execute();

    assertThat(exitCode).isZero();
    assertThat(stdout.toString())
        .contains("Expenses synced successfully.")
        .contains("Bucket: my-bucket")
        .contains("Object: expenses.json");
    verify(useCase).execute();
  }

  @Test
  void shouldDisplayErrorMessageOnSyncException() {
    when(useCase.execute()).thenThrow(new SyncException("Failed to upload"));

    int exitCode = execute();

    assertThat(exitCode).isEqualTo(1);
    assertThat(stderr.toString()).contains("Error: Failed to upload");
  }

  @Test
  void shouldDisplayHelpAndExitSuccessfully() {
    int exitCode = execute("--help");

    assertThat(exitCode).isZero();
    assertThat(stdout.toString()).contains("Usage: push");
  }
}
