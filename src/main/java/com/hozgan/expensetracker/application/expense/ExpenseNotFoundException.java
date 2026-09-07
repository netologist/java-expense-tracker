package com.hozgan.expensetracker.application.expense;

import java.io.Serial;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ExpenseNotFoundException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final UUID id;

  public ExpenseNotFoundException(UUID id) {
    super("Expense not found: " + id);
    this.id = id;
  }
}
