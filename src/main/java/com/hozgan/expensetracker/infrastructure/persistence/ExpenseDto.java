package com.hozgan.expensetracker.infrastructure.persistence;

import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.javamoney.moneta.Money;

public record ExpenseDto(
    UUID id,
    String amount,
    String currency,
    ExpenseCategory category,
    String description,
    String date) {

  public static ExpenseDto fromDomain(Expense expense) {
    Objects.requireNonNull(expense, "expense must not be null");
    BigDecimal amountNumber = expense.amount().getNumber().numberValue(BigDecimal.class);
    String currencyCode = expense.amount().getCurrency().getCurrencyCode();
    return new ExpenseDto(
        expense.id(),
        amountNumber.toPlainString(),
        currencyCode,
        expense.category(),
        expense.description(),
        expense.date().toString());
  }

  public Expense toDomain() {
    var monetaryAmount = Money.of(new BigDecimal(amount), currency);
    return new Expense(id, monetaryAmount, category, description, LocalDate.parse(date));
  }
}
