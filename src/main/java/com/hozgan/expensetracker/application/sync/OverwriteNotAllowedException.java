package com.hozgan.expensetracker.application.sync;

import java.io.Serial;

public class OverwriteNotAllowedException extends SyncException {

  @Serial private static final long serialVersionUID = 1L;

  public OverwriteNotAllowedException(String message) {
    super(message);
  }
}
