package com.hozgan.expensetracker.application.expense;

import com.hozgan.expensetracker.domain.expense.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

public record AddExpenseCommand(
    BigDecimal amount,
    Currency currency,
    ExpenseCategory category,
    String description,
    LocalDate date) {

  public AddExpenseCommand {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(currency, "currency must not be null");
    Objects.requireNonNull(category, "category must not be null");
    Objects.requireNonNull(description, "description must not be null");
    Objects.requireNonNull(date, "date must not be null");
  }
}
