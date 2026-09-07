package com.hozgan.expensetracker.application.sync;

public class OverwriteNotAllowedException extends SyncException {

  private static final long serialVersionUID = 1L;

  public OverwriteNotAllowedException(String message) {
    super(message);
  }
}
