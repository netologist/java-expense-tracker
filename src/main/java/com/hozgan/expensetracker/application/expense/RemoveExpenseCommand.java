package com.hozgan.expensetracker.application.expense;

import java.util.Objects;
import java.util.UUID;

public record RemoveExpenseCommand(UUID id) {

  public RemoveExpenseCommand {
    Objects.requireNonNull(id, "id must not be null");
  }
}
