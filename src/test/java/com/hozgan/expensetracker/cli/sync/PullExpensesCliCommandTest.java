package com.hozgan.expensetracker.cli.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hozgan.expensetracker.application.sync.OverwriteNotAllowedException;
import com.hozgan.expensetracker.application.sync.PullExpensesUseCase;
import com.hozgan.expensetracker.application.sync.SyncException;
import com.hozgan.expensetracker.application.sync.SyncResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class PullExpensesCliCommandTest {

  private final PullExpensesUseCase useCase = mock(PullExpensesUseCase.class);
  private final StringWriter stdout = new StringWriter();
  private final StringWriter stderr = new StringWriter();

  private int execute(String... args) {
    var commandLine = new CommandLine(new PullExpensesCliCommand(useCase));
    commandLine.setOut(new PrintWriter(stdout, true));
    commandLine.setErr(new PrintWriter(stderr, true));
    return commandLine.execute(args);
  }

  @Test
  void shouldDisplaySuccessMessageOnSuccessfulPullWithoutForce() {
    when(useCase.execute(false)).thenReturn(new SyncResult("my-bucket", "expenses.json"));

    int exitCode = execute();

    assertThat(exitCode).isZero();
    assertThat(stdout.toString())
        .contains("Expenses synced successfully.")
        .contains("Bucket: my-bucket")
        .contains("Object: expenses.json");
    verify(useCase).execute(false);
  }

  @Test
  void shouldDisplaySuccessMessageOnSuccessfulPullWithForce() {
    when(useCase.execute(true)).thenReturn(new SyncResult("my-bucket", "expenses.json"));

    int exitCode = execute("--force");

    assertThat(exitCode).isZero();
    assertThat(stdout.toString())
        .contains("Expenses synced successfully.")
        .contains("Bucket: my-bucket")
        .contains("Object: expenses.json");
    verify(useCase).execute(true);
  }

  @Test
  void shouldSupportShortForceOption() {
    when(useCase.execute(true)).thenReturn(new SyncResult("my-bucket", "expenses.json"));

    int exitCode = execute("-f");

    assertThat(exitCode).isZero();
    verify(useCase).execute(true);
  }

  @Test
  void shouldDisplayOverwriteProtectionMessageWhenOverwriteNotAllowed() {
    when(useCase.execute(false))
        .thenThrow(
            new OverwriteNotAllowedException(
                "Local data file already exists.\n\nUse --force to overwrite it."));

    int exitCode = execute();

    assertThat(exitCode).isEqualTo(1);
    assertThat(stderr.toString())
        .contains("Local data file already exists.")
        .contains("Use --force to overwrite it.");
  }

  @Test
  void shouldDisplayErrorMessageOnSyncException() {
    when(useCase.execute(false)).thenThrow(new SyncException("Connection failed"));

    int exitCode = execute();

    assertThat(exitCode).isEqualTo(1);
    assertThat(stderr.toString()).contains("Error: Connection failed");
  }

  @Test
  void shouldDisplayHelpAndExitSuccessfully() {
    int exitCode = execute("--help");

    assertThat(exitCode).isZero();
    assertThat(stdout.toString()).contains("Usage: pull");
  }
}
