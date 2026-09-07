package com.hozgan.expensetracker.domain.expense;

public class ExpensePersistenceException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ExpensePersistenceException(String message) {
    super(message);
  }

  public ExpensePersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
