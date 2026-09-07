package com.hozgan.expensetracker.application.sync;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PushExpensesUseCase {

  private final ExpenseSyncPort expenseSyncPort;
  private final Path localFilePath;

  public SyncResult execute() {
    Objects.requireNonNull(localFilePath, "localFilePath must not be null");
    if (!Files.exists(localFilePath)) {
      throw new SyncException("Local data file does not exist: " + localFilePath);
    }
    expenseSyncPort.upload(localFilePath);
    return new SyncResult(expenseSyncPort.getBucket(), expenseSyncPort.getObjectKey());
  }
}
