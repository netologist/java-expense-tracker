package com.hozgan.expensetracker.application.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpenseCategory;
import com.hozgan.expensetracker.domain.expense.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Currency;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AddExpenseUseCaseTest {

  private final ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
  private final AddExpenseUseCase useCase = new AddExpenseUseCase(expenseRepository);

  @Test
  void shouldCreateAndSaveExpense() {
    var command =
        new AddExpenseCommand(
            new BigDecimal("12.50"),
            Currency.getInstance("GBP"),
            ExpenseCategory.FOOD,
            "Lunch",
            LocalDate.of(2026, 9, 7));

    var expense = useCase.execute(command);

    assertThat(expense.id()).isNotNull();
    assertThat(expense.amount()).isEqualTo(Money.of(new BigDecimal("12.50"), "GBP"));
    assertThat(expense.category()).isEqualTo(ExpenseCategory.FOOD);
    assertThat(expense.description()).isEqualTo("Lunch");
    assertThat(expense.date()).isEqualTo(LocalDate.of(2026, 9, 7));

    var captor = ArgumentCaptor.forClass(Expense.class);
    verify(expenseRepository).save(captor.capture());
    assertThat(captor.getValue()).isSameAs(expense);
  }

  @Test
  void shouldGenerateANewIdForEachExpense() {
    var command =
        new AddExpenseCommand(
            new BigDecimal("4.75"),
            Currency.getInstance("GBP"),
            ExpenseCategory.TRANSPORT,
            "Bus ticket",
            LocalDate.now(ZoneId.systemDefault()));

    var first = useCase.execute(command);
    var second = useCase.execute(command);

    assertThat(second.id()).isNotEqualTo(first.id());
  }

  @Test
  void shouldNotSaveExpenseWithNonPositiveAmount() {
    var command =
        new AddExpenseCommand(
            BigDecimal.ZERO,
            Currency.getInstance("GBP"),
            ExpenseCategory.FOOD,
            "Lunch",
            LocalDate.now(ZoneId.systemDefault()));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> useCase.execute(command))
        .withMessage("amount must be greater than zero");

    verify(expenseRepository, never()).save(any());
  }

  @Test
  void shouldNotSaveExpenseWithNegativeAmount() {
    var command =
        new AddExpenseCommand(
            new BigDecimal("-10.00"),
            Currency.getInstance("GBP"),
            ExpenseCategory.FOOD,
            "Lunch",
            LocalDate.now(ZoneId.systemDefault()));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> useCase.execute(command))
        .withMessage("amount must be greater than zero");

    verify(expenseRepository, never()).save(any());
  }

  @Test
  void shouldNotSaveExpenseWithBlankDescription() {
    var command =
        new AddExpenseCommand(
            new BigDecimal("10.00"),
            Currency.getInstance("GBP"),
            ExpenseCategory.FOOD,
            "   ",
            LocalDate.now(ZoneId.systemDefault()));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> useCase.execute(command))
        .withMessage("description must not be blank");

    verify(expenseRepository, never()).save(any());
  }

  @Test
  void shouldRejectNullCommand() {
    assertThatNullPointerException()
        .isThrownBy(() -> useCase.execute(null))
        .withMessage("command must not be null");

    verify(expenseRepository, never()).save(any());
  }
}
