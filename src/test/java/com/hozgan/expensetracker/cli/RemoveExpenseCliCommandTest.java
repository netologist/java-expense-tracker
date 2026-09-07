package com.hozgan.expensetracker.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.hozgan.expensetracker.application.expense.ExpenseNotFoundException;
import com.hozgan.expensetracker.application.expense.RemoveExpenseCommand;
import com.hozgan.expensetracker.application.expense.RemoveExpenseUseCase;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class RemoveExpenseCliCommandTest {

  private final RemoveExpenseUseCase useCase = mock(RemoveExpenseUseCase.class);
  private final StringWriter stdout = new StringWriter();
  private final StringWriter stderr = new StringWriter();

  private int execute(String... args) {
    var commandLine = new CommandLine(new RemoveExpenseCliCommand(useCase));
    commandLine.setOut(new PrintWriter(stdout, true));
    commandLine.setErr(new PrintWriter(stderr, true));
    return commandLine.execute(args);
  }

  @Test
  void shouldRemoveExpenseSuccessfullyWithLongOption() {
    var id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    doNothing().when(useCase).execute(new RemoveExpenseCommand(id));

    int exitCode = execute("--id", id.toString());

    assertThat(exitCode).isZero();
    assertThat(stdout.toString()).contains("Expense removed successfully.").contains("ID: " + id);
    verify(useCase).execute(new RemoveExpenseCommand(id));
  }

  @Test
  void shouldRemoveExpenseSuccessfullyWithShortOption() {
    var id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    doNothing().when(useCase).execute(new RemoveExpenseCommand(id));

    int exitCode = execute("-i", id.toString());

    assertThat(exitCode).isZero();
    assertThat(stdout.toString()).contains("Expense removed successfully.").contains("ID: " + id);
    verify(useCase).execute(new RemoveExpenseCommand(id));
  }

  @Test
  void shouldDisplayErrorMessageWhenExpenseNotFound() {
    var id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    doThrow(new ExpenseNotFoundException(id)).when(useCase).execute(any());

    int exitCode = execute("--id", id.toString());

    assertThat(exitCode).isEqualTo(1);
    assertThat(stderr.toString()).contains("Expense not found: " + id);
  }

  @Test
  void shouldRejectInvalidUuid() {
    int exitCode = execute("--id", "invalid-id");

    assertThat(exitCode).isEqualTo(2);
    assertThat(stderr.toString()).contains("Invalid value for option '--id'");
  }

  @Test
  void shouldRejectMissingIdOption() {
    int exitCode = execute();

    assertThat(exitCode).isEqualTo(2);
    assertThat(stderr.toString()).contains("Missing required option");
  }

  @Test
  void shouldDisplayHelpAndExitSuccessfully() {
    int exitCode = execute("--help");

    assertThat(exitCode).isZero();
    assertThat(stdout.toString()).contains("Usage: remove");
  }
}
