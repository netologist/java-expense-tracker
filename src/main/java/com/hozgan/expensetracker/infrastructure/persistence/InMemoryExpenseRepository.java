package com.hozgan.expensetracker.infrastructure.persistence;

import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpenseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryExpenseRepository implements ExpenseRepository {

  private final List<Expense> expenses = new ArrayList<>();

  @Override
  public synchronized void save(Expense expense) {
    expenses.removeIf(e -> e.id().equals(expense.id()));
    expenses.add(expense);
  }

  @Override
  public synchronized List<Expense> findAll() {
    return List.copyOf(expenses);
  }

  @Override
  public synchronized boolean deleteById(UUID id) {
    return expenses.removeIf(e -> e.id().equals(id));
  }

  @Override
  public synchronized Optional<Expense> findById(UUID id) {
    return expenses.stream().filter(e -> e.id().equals(id)).findFirst();
  }
}
