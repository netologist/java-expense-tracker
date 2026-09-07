package com.hozgan.expensetracker.application.sync;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PullExpensesUseCase {

  private final ExpenseSyncPort expenseSyncPort;
  private final Path localFilePath;

  public SyncResult execute(boolean force) {
    Objects.requireNonNull(localFilePath, "localFilePath must not be null");
    if (Files.exists(localFilePath) && !force) {
      throw new OverwriteNotAllowedException(
          "Local data file already exists.\n\nUse --force to overwrite it.");
    }
    expenseSyncPort.download(localFilePath, force);
    return new SyncResult(expenseSyncPort.getBucket(), expenseSyncPort.getObjectKey());
  }
}
