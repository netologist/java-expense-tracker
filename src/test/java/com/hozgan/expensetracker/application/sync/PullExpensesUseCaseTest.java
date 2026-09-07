package com.hozgan.expensetracker.application.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PullExpensesUseCaseTest {

  @TempDir Path tempDir;

  private final ExpenseSyncPort syncPort = mock(ExpenseSyncPort.class);

  @Test
  void shouldDownloadSuccessfullyWhenLocalFileDoesNotExist() {
    Path localFile = tempDir.resolve("expenses.json");

    when(syncPort.getBucket()).thenReturn("my-bucket");
    when(syncPort.getObjectKey()).thenReturn("expenses.json");

    var useCase = new PullExpensesUseCase(syncPort, localFile);
    SyncResult result = useCase.execute(false);

    assertThat(result.bucket()).isEqualTo("my-bucket");
    assertThat(result.objectKey()).isEqualTo("expenses.json");
    verify(syncPort).download(localFile, false);
  }

  @Test
  void shouldThrowOverwriteNotAllowedExceptionWhenLocalFileExistsWithoutForce() throws IOException {
    Path localFile = tempDir.resolve("expenses.json");
    Files.writeString(localFile, "[]");

    var useCase = new PullExpensesUseCase(syncPort, localFile);

    assertThatThrownBy(() -> useCase.execute(false))
        .isInstanceOf(OverwriteNotAllowedException.class)
        .hasMessageContaining("Local data file already exists.")
        .hasMessageContaining("Use --force to overwrite it.");
  }

  @Test
  void shouldDownloadSuccessfullyWhenLocalFileExistsWithForce() throws IOException {
    Path localFile = tempDir.resolve("expenses.json");
    Files.writeString(localFile, "[]");

    when(syncPort.getBucket()).thenReturn("my-bucket");
    when(syncPort.getObjectKey()).thenReturn("expenses.json");

    var useCase = new PullExpensesUseCase(syncPort, localFile);
    SyncResult result = useCase.execute(true);

    assertThat(result.bucket()).isEqualTo("my-bucket");
    assertThat(result.objectKey()).isEqualTo("expenses.json");
    verify(syncPort).download(localFile, true);
  }
}
