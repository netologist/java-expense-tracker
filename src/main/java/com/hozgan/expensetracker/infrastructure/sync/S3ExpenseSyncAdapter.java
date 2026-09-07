package com.hozgan.expensetracker.infrastructure.sync;

import com.hozgan.expensetracker.application.sync.ExpenseSyncPort;
import com.hozgan.expensetracker.application.sync.OverwriteNotAllowedException;
import com.hozgan.expensetracker.application.sync.SyncException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public final class S3ExpenseSyncAdapter implements ExpenseSyncPort, AutoCloseable {

  private final S3Client s3Client;
  private final String bucket;
  private final String objectKey;

  public S3ExpenseSyncAdapter(S3ExpenseSyncConfig config) {
    this(createS3Client(config), config.bucket(), config.objectKey());
  }

  public S3ExpenseSyncAdapter(S3Client s3Client, String bucket, String objectKey) {
    this.s3Client = Objects.requireNonNull(s3Client, "s3Client must not be null");
    this.bucket = Objects.requireNonNull(bucket, "bucket must not be null");
    this.objectKey = Objects.requireNonNull(objectKey, "objectKey must not be null");
  }

  public static S3Client createS3Client(S3ExpenseSyncConfig config) {
    S3ClientBuilder builder = S3Client.builder().region(Region.of(config.region()));

    if (config.endpoint() != null) {
      builder.endpointOverride(config.endpoint());
      builder.forcePathStyle(true);
    }

    if (config.accessKey() != null && config.secretKey() != null) {
      builder.credentialsProvider(
          StaticCredentialsProvider.create(
              AwsBasicCredentials.create(config.accessKey(), config.secretKey())));
    } else {
      builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
    }

    return builder.build();
  }

  @Override
  public void upload(Path localFile) {
    Objects.requireNonNull(localFile, "localFile must not be null");
    if (!Files.exists(localFile)) {
      throw new SyncException("Local data file does not exist: " + localFile);
    }

    try {
      var putRequest = PutObjectRequest.builder().bucket(bucket).key(objectKey).build();
      s3Client.putObject(putRequest, RequestBody.fromFile(localFile));
    } catch (S3Exception e) {
      String msg =
          e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
      throw new SyncException("Failed to upload expenses to S3 bucket '" + bucket + "': " + msg, e);
    } catch (Exception e) {
      throw new SyncException("Failed to upload expenses to S3: " + e.getMessage(), e);
    }
  }

  @Override
  public void download(Path targetFile, boolean overwrite) {
    Objects.requireNonNull(targetFile, "targetFile must not be null");
    if (Files.exists(targetFile) && !overwrite) {
      throw new OverwriteNotAllowedException(
          "Local data file already exists.\n\nUse --force to overwrite it.");
    }

    try {
      Path parent = targetFile.getParent();
      if (parent != null && !Files.exists(parent)) {
        Files.createDirectories(parent);
      }
      Path tempFile =
          parent != null
              ? Files.createTempFile(parent, "download-expenses", ".tmp")
              : Files.createTempFile("download-expenses", ".tmp");
      Files.deleteIfExists(tempFile);
      try {
        var getRequest = GetObjectRequest.builder().bucket(bucket).key(objectKey).build();
        s3Client.getObject(getRequest, tempFile);
        Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
      } finally {
        Files.deleteIfExists(tempFile);
      }
    } catch (NoSuchKeyException e) {
      throw new SyncException("Object not found in S3 bucket '" + bucket + "': " + objectKey, e);
    } catch (S3Exception e) {
      String msg =
          e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
      throw new SyncException(
          "Failed to download expenses from S3 bucket '" + bucket + "': " + msg, e);
    } catch (OverwriteNotAllowedException e) {
      throw e;
    } catch (IOException e) {
      throw new SyncException("Failed to write downloaded expenses to " + targetFile, e);
    } catch (Exception e) {
      throw new SyncException("Failed to download expenses from S3: " + e.getMessage(), e);
    }
  }

  @Override
  public String getBucket() {
    return bucket;
  }

  @Override
  public String getObjectKey() {
    return objectKey;
  }

  @Override
  public void close() {
    s3Client.close();
  }
}
