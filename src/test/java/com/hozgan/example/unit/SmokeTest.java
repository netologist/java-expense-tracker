package com.hozgan.example.unit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class SmokeTest {
  @Test
  void shouldRunUnitTests() {
    assertThat(2 + 2).isEqualTo(4);
  }
}
