package com.hozgan.expensetracker.domain.expense;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import javax.money.MonetaryAmount;

public record Expense(
    UUID id, MonetaryAmount amount, ExpenseCategory category, String description, LocalDate date) {

  public Expense {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(category, "category must not be null");
    Objects.requireNonNull(description, "description must not be null");
    Objects.requireNonNull(date, "date must not be null");

    if (amount.isNegativeOrZero()) {
      throw new IllegalArgumentException("amount must be greater than zero");
    }

    if (description.isBlank()) {
      throw new IllegalArgumentException("description must not be blank");
    }
  }
}
