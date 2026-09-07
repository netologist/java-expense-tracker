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

class PushExpensesUseCaseTest {

  @TempDir Path tempDir;

  private final ExpenseSyncPort syncPort = mock(ExpenseSyncPort.class);

  @Test
  void shouldUploadLocalFileSuccessfully() throws IOException {
    Path localFile = tempDir.resolve("expenses.json");
    Files.writeString(localFile, "[]");

    when(syncPort.getBucket()).thenReturn("my-bucket");
    when(syncPort.getObjectKey()).thenReturn("expenses.json");

    var useCase = new PushExpensesUseCase(syncPort, localFile);
    SyncResult result = useCase.execute();

    assertThat(result.bucket()).isEqualTo("my-bucket");
    assertThat(result.objectKey()).isEqualTo("expenses.json");
    verify(syncPort).upload(localFile);
  }

  @Test
  void shouldThrowSyncExceptionWhenLocalFileDoesNotExist() {
    Path localFile = tempDir.resolve("non-existent.json");

    var useCase = new PushExpensesUseCase(syncPort, localFile);

    assertThatThrownBy(useCase::execute)
        .isInstanceOf(SyncException.class)
        .hasMessageContaining("Local data file does not exist");
  }
}
