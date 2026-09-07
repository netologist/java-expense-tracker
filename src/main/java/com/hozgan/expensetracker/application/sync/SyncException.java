package com.hozgan.expensetracker.application.sync;

import java.io.Serial;

public class SyncException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  public SyncException(String message) {
    super(message);
  }

  public SyncException(String message, Throwable cause) {
    super(message, cause);
  }
}
