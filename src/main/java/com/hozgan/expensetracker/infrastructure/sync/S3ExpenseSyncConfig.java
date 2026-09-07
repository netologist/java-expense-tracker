package com.hozgan.expensetracker.infrastructure.sync;

import java.net.URI;
import java.util.Objects;

public record S3ExpenseSyncConfig(
    String bucket,
    String region,
    URI endpoint,
    String accessKey,
    String secretKey,
    String objectKey) {

  public static final String DEFAULT_OBJECT_KEY = "expenses.json";
  public static final String DEFAULT_REGION = "us-east-1";

  public S3ExpenseSyncConfig {
    Objects.requireNonNull(bucket, "bucket must not be null");
    if (bucket.isBlank()) {
      throw new IllegalArgumentException("bucket must not be blank");
    }
    if (region == null || region.isBlank()) {
      region = DEFAULT_REGION;
    }
    if (objectKey == null || objectKey.isBlank()) {
      objectKey = DEFAULT_OBJECT_KEY;
    }
  }

  public static S3ExpenseSyncConfig fromEnvironment() {
    String bucket = getPropertyOrEnv("expense.tracker.s3.bucket", "EXPENSE_TRACKER_S3_BUCKET");
    if (bucket == null || bucket.isBlank()) {
      bucket = "expense-tracker";
    }

    String region = getPropertyOrEnv("expense.tracker.aws.region", "EXPENSE_TRACKER_AWS_REGION");
    if (region == null || region.isBlank()) {
      region = DEFAULT_REGION;
    }

    String endpointStr =
        getPropertyOrEnv("expense.tracker.s3.endpoint", "EXPENSE_TRACKER_S3_ENDPOINT");
    URI endpoint = (endpointStr != null && !endpointStr.isBlank()) ? URI.create(endpointStr) : null;

    String accessKey =
        getPropertyOrEnv("expense.tracker.s3.access.key", "EXPENSE_TRACKER_S3_ACCESS_KEY");
    String secretKey =
        getPropertyOrEnv("expense.tracker.s3.secret.key", "EXPENSE_TRACKER_S3_SECRET_KEY");

    String objectKey =
        getPropertyOrEnv("expense.tracker.s3.object.key", "EXPENSE_TRACKER_S3_OBJECT_KEY");
    if (objectKey == null || objectKey.isBlank()) {
      objectKey = DEFAULT_OBJECT_KEY;
    }

    return new S3ExpenseSyncConfig(bucket, region, endpoint, accessKey, secretKey, objectKey);
  }

  private static String getPropertyOrEnv(String propertyName, String envName) {
    String value = System.getProperty(propertyName);
    if (value != null && !value.isBlank()) {
      return value.trim();
    }
    value = System.getenv(envName);
    if (value != null && !value.isBlank()) {
      return value.trim();
    }
    return null;
  }
}
