package com.hozgan.expensetracker.application.expense;

import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpenseRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ListExpensesUseCase {

  private final ExpenseRepository expenseRepository;

  public List<Expense> execute() {
    return expenseRepository.findAll().stream()
        .sorted(
            Comparator.comparing(Expense::date, Comparator.reverseOrder())
                .thenComparing(Expense::id))
        .toList();
  }
}
