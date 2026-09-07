package com.hozgan.expensetracker.application.expense;

import java.util.UUID;

public class ExpenseNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final UUID id;

  public ExpenseNotFoundException(UUID id) {
    super("Expense not found: " + id);
    this.id = id;
  }

  public UUID getId() {
    return id;
  }
}
