package com.hozgan.expensetracker.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SmokeIT {
    @Test
    void shouldRunUnitTests() {
        assertThat("integration").startsWith("integ");
    }
}