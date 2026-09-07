package com.hozgan.expensetracker.domain.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import javax.money.MonetaryAmount;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;

class ExpenseTest {

  @Test
  void shouldCreateExpense() {
    var id = UUID.randomUUID();
    MonetaryAmount amount = Money.of(new BigDecimal("12.50"), "GBP");
    var date = LocalDate.of(2026, 9, 7);

    var expense = new Expense(id, amount, ExpenseCategory.FOOD, "Lunch", date);

    assertThat(expense.id()).isEqualTo(id);
    assertThat(expense.amount()).isEqualTo(amount);
    assertThat(expense.category()).isEqualTo(ExpenseCategory.FOOD);
    assertThat(expense.description()).isEqualTo("Lunch");
    assertThat(expense.date()).isEqualTo(date);
  }

  @Test
  void shouldRejectZeroAmount() {
    MonetaryAmount amount = Money.of(BigDecimal.ZERO, "GBP");

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new Expense(
                    UUID.randomUUID(),
                    amount,
                    ExpenseCategory.FOOD,
                    "Lunch",
                    LocalDate.now(ZoneId.systemDefault())))
        .withMessage("amount must be greater than zero");
  }

  @Test
  void shouldRejectNegativeAmount() {
    MonetaryAmount amount = Money.of(new BigDecimal("-12.50"), "GBP");

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new Expense(
                    UUID.randomUUID(),
                    amount,
                    ExpenseCategory.FOOD,
                    "Lunch",
                    LocalDate.now(ZoneId.systemDefault())))
        .withMessage("amount must be greater than zero");
  }

  @Test
  void shouldRejectBlankDescription() {
    MonetaryAmount amount = Money.of(new BigDecimal("12.50"), "GBP");

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new Expense(
                    UUID.randomUUID(),
                    amount,
                    ExpenseCategory.FOOD,
                    " ",
                    LocalDate.now(ZoneId.systemDefault())))
        .withMessage("description must not be blank");
  }

  @Test
  void shouldRejectNullAmount() {
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                new Expense(
                    UUID.randomUUID(),
                    null,
                    ExpenseCategory.FOOD,
                    "Lunch",
                    LocalDate.now(ZoneId.systemDefault())))
        .withMessage("amount must not be null");
  }

  @Test
  void shouldRejectNullDate() {
    MonetaryAmount amount = Money.of(new BigDecimal("12.50"), "GBP");

    assertThatNullPointerException()
        .isThrownBy(
            () -> new Expense(UUID.randomUUID(), amount, ExpenseCategory.FOOD, "Lunch", null))
        .withMessage("date must not be null");
  }
}
