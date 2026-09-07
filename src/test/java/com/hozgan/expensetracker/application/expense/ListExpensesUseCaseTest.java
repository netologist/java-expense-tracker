package com.hozgan.expensetracker.application.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpenseCategory;
import com.hozgan.expensetracker.domain.expense.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

class ListExpensesUseCaseTest {

  private final ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
  private final ListExpensesUseCase useCase = new ListExpensesUseCase(expenseRepository);

  @Test
  void shouldReturnEmptyListWhenNoExpenses() {
    when(expenseRepository.findAll()).thenReturn(List.of());

    List<Expense> expenses = useCase.execute();

    assertThat(expenses).isEmpty();
  }

  @Test
  void shouldReturnExpensesSortedByDateDescendingThenById() {
    var id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    var id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    var id3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    var expenseOld =
        new Expense(
            id1,
            Money.of(new BigDecimal("10.00"), "GBP"),
            ExpenseCategory.FOOD,
            "Older",
            LocalDate.of(2026, 9, 1));
    var expenseNewSameDate1 =
        new Expense(
            id2,
            Money.of(new BigDecimal("20.00"), "GBP"),
            ExpenseCategory.TRANSPORT,
            "Newer 1",
            LocalDate.of(2026, 9, 7));
    var expenseNewSameDate2 =
        new Expense(
            id3,
            Money.of(new BigDecimal("30.00"), "GBP"),
            ExpenseCategory.ENTERTAINMENT,
            "Newer 2",
            LocalDate.of(2026, 9, 7));

    // Input in unordered order
    when(expenseRepository.findAll())
        .thenReturn(List.of(expenseOld, expenseNewSameDate2, expenseNewSameDate1));

    List<Expense> result = useCase.execute();

    assertThat(result).containsExactly(expenseNewSameDate1, expenseNewSameDate2, expenseOld);
  }
}
