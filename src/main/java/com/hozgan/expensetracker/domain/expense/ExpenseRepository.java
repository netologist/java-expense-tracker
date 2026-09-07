package com.hozgan.expensetracker.domain.expense;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository {

  void save(Expense expense);

  List<Expense> findAll();

  boolean deleteById(UUID id);

  Optional<Expense> findById(UUID id);
}
