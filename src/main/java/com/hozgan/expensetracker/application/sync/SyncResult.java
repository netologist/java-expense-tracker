package com.hozgan.expensetracker.application.sync;

import java.util.Objects;

public record SyncResult(String bucket, String objectKey) {

  public SyncResult {
    Objects.requireNonNull(bucket, "bucket must not be null");
    Objects.requireNonNull(objectKey, "objectKey must not be null");
  }
}
