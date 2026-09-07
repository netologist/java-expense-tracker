package com.hozgan.expensetracker.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hozgan.expensetracker.application.sync.OverwriteNotAllowedException;
import com.hozgan.expensetracker.infrastructure.sync.S3ExpenseSyncAdapter;
import com.hozgan.expensetracker.infrastructure.sync.S3ExpenseSyncConfig;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@Testcontainers
class S3ExpenseSyncAdapterIT {

  private static final String BUCKET = "test-bucket";
  private static final String OBJECT_KEY = "expenses.json";

  @Container
  static final MinIOContainer minio =
      new MinIOContainer("minio/minio:RELEASE.2024-01-18T22-51-28Z");

  private static S3Client s3Client;
  private static S3ExpenseSyncAdapter adapter;

  @TempDir Path tempDir;

  @BeforeAll
  static void setUp() {
    s3Client =
        S3Client.builder()
            .endpointOverride(URI.create(minio.getS3URL()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(minio.getUserName(), minio.getPassword())))
            .region(Region.US_EAST_1)
            .forcePathStyle(true)
            .build();

    s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());

    adapter = new S3ExpenseSyncAdapter(s3Client, BUCKET, OBJECT_KEY);
  }

  @AfterAll
  static void tearDown() {
    if (s3Client != null) {
      s3Client.close();
    }
  }

  @Test
  void shouldUploadLocalFileToMinio() throws IOException {
    Path localFile = tempDir.resolve("upload.json");
    String content = "[{\"id\":\"test-id\",\"amount\":\"12.50\"}]";
    Files.writeString(localFile, content);

    adapter.upload(localFile);

    Path downloadedFile = tempDir.resolve("downloaded-after-upload.json");
    adapter.download(downloadedFile, false);

    assertThat(Files.readString(downloadedFile)).isEqualTo(content);
  }

  @Test
  void shouldDownloadExistingObjectFromMinio() throws IOException {
    Path localFile = tempDir.resolve("seed.json");
    String content = "[{\"id\":\"seed-id\",\"amount\":\"45.00\"}]";
    Files.writeString(localFile, content);
    adapter.upload(localFile);

    Path targetFile = tempDir.resolve("target.json");
    adapter.download(targetFile, false);

    assertThat(Files.exists(targetFile)).isTrue();
    assertThat(Files.readString(targetFile)).isEqualTo(content);
  }

  @Test
  void shouldEnforceOverwriteSafetyPolicy() throws IOException {
    Path localFile = tempDir.resolve("overwrite-test.json");
    Files.writeString(localFile, "existing local content");

    assertThatThrownBy(() -> adapter.download(localFile, false))
        .isInstanceOf(OverwriteNotAllowedException.class)
        .hasMessageContaining("Local data file already exists.")
        .hasMessageContaining("Use --force to overwrite it.");

    // Local content untouched
    assertThat(Files.readString(localFile)).isEqualTo("existing local content");

    // Upload new content to S3
    Path s3Source = tempDir.resolve("s3-source.json");
    String newS3Content = "new s3 content";
    Files.writeString(s3Source, newS3Content);
    adapter.upload(s3Source);

    // Download with force = true
    adapter.download(localFile, true);
    assertThat(Files.readString(localFile)).isEqualTo(newS3Content);
  }
}
