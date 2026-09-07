package com.hozgan.expensetracker.application.expense;

import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpenseRepository;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;

@RequiredArgsConstructor
public final class AddExpenseUseCase {

  private final ExpenseRepository expenseRepository;

  public Expense execute(AddExpenseCommand command) {
    Objects.requireNonNull(command, "command must not be null");
    var amount = Money.of(command.amount(), command.currency().getCurrencyCode());

    var expense =
        new Expense(
            UUID.randomUUID(), amount, command.category(), command.description(), command.date());

    expenseRepository.save(expense);

    return expense;
  }
}
