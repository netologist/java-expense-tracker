package com.hozgan.expensetracker.infrastructure.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import org.junit.jupiter.api.Test;

class S3ExpenseSyncConfigTest {

  @Test
  void shouldLoadConfigFromDotenv() {
    var dotenv = mock(Dotenv.class);
    when(dotenv.get("EXPENSE_TRACKER_S3_BUCKET")).thenReturn("custom-bucket");
    when(dotenv.get("EXPENSE_TRACKER_AWS_REGION")).thenReturn("eu-central-1");
    when(dotenv.get("EXPENSE_TRACKER_S3_ENDPOINT")).thenReturn("http://localhost:9000");
    when(dotenv.get("EXPENSE_TRACKER_S3_ACCESS_KEY")).thenReturn("test-access");
    when(dotenv.get("EXPENSE_TRACKER_S3_SECRET_KEY")).thenReturn("test-secret");
    when(dotenv.get("EXPENSE_TRACKER_S3_OBJECT_KEY")).thenReturn("data/my-expenses.json");

    var config = S3ExpenseSyncConfig.fromEnvironment(dotenv);

    assertThat(config.bucket()).isEqualTo("custom-bucket");
    assertThat(config.region()).isEqualTo("eu-central-1");
    assertThat(config.endpoint()).isEqualTo(URI.create("http://localhost:9000"));
    assertThat(config.accessKey()).isEqualTo("test-access");
    assertThat(config.secretKey()).isEqualTo("test-secret");
    assertThat(config.objectKey()).isEqualTo("data/my-expenses.json");
  }

  @Test
  void shouldApplyDefaultsWhenVariablesAreNotSet() {
    var dotenv = mock(Dotenv.class);

    var config = S3ExpenseSyncConfig.fromEnvironment(dotenv);

    assertThat(config.bucket()).isEqualTo("expense-tracker");
    assertThat(config.region()).isEqualTo("us-east-1");
    assertThat(config.endpoint()).isNull();
    assertThat(config.accessKey()).isNull();
    assertThat(config.secretKey()).isNull();
    assertThat(config.objectKey()).isEqualTo("expenses.json");
  }

  @Test
  void shouldPrioritizeSystemPropertiesOverDotenv() {
    var dotenv = mock(Dotenv.class);
    when(dotenv.get("EXPENSE_TRACKER_S3_BUCKET")).thenReturn("dotenv-bucket");

    System.setProperty("expense.tracker.s3.bucket", "sysprop-bucket");
    try {
      var config = S3ExpenseSyncConfig.fromEnvironment(dotenv);
      assertThat(config.bucket()).isEqualTo("sysprop-bucket");
    } finally {
      System.clearProperty("expense.tracker.s3.bucket");
    }
  }

  @Test
  void shouldRejectBlankBucket() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new S3ExpenseSyncConfig("   ", "us-east-1", null, null, null, "key"));
  }
}
