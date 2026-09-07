package com.hozgan.expensetracker.domain.expense;

import java.io.Serial;

public class ExpensePersistenceException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  public ExpensePersistenceException(String message) {
    super(message);
  }

  public ExpensePersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
