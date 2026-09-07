package com.hozgan.expensetracker.application.expense;

import com.hozgan.expensetracker.domain.expense.ExpenseRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RemoveExpenseUseCase {

  private final ExpenseRepository expenseRepository;

  public void execute(RemoveExpenseCommand command) {
    Objects.requireNonNull(command, "command must not be null");
    boolean removed = expenseRepository.deleteById(command.id());
    if (!removed) {
      throw new ExpenseNotFoundException(command.id());
    }
  }
}
