package com.hozgan.expensetracker.infrastructure.persistence;

import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpenseRepository;
import java.util.ArrayList;
import java.util.List;

public final class InMemoryExpenseRepository implements ExpenseRepository {

  private final List<Expense> expenses = new ArrayList<>();

  @Override
  public void save(Expense expense) {
    expenses.add(expense);
  }
}
