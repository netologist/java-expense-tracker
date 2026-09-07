package com.hozgan.expensetracker.application.expense;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hozgan.expensetracker.domain.expense.ExpenseRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RemoveExpenseUseCaseTest {

  private final ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
  private final RemoveExpenseUseCase useCase = new RemoveExpenseUseCase(expenseRepository);

  @Test
  void shouldRemoveExistingExpense() {
    var id = UUID.randomUUID();
    when(expenseRepository.deleteById(id)).thenReturn(true);

    useCase.execute(new RemoveExpenseCommand(id));

    verify(expenseRepository).deleteById(id);
  }

  @Test
  void shouldThrowExpenseNotFoundExceptionWhenExpenseDoesNotExist() {
    var id = UUID.randomUUID();
    when(expenseRepository.deleteById(id)).thenReturn(false);

    assertThatThrownBy(() -> useCase.execute(new RemoveExpenseCommand(id)))
        .isInstanceOf(ExpenseNotFoundException.class)
        .hasMessage("Expense not found: " + id)
        .extracting(e -> ((ExpenseNotFoundException) e).getId())
        .isEqualTo(id);
  }

  @Test
  void shouldRejectNullCommand() {
    assertThatNullPointerException()
        .isThrownBy(() -> useCase.execute(null))
        .withMessage("command must not be null");
  }
}
