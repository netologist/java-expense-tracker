package com.hozgan.expensetracker.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpenseCategory;
import com.hozgan.expensetracker.infrastructure.persistence.JsonExpenseRepository;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonExpenseRepositoryIT {

  @TempDir Path tempDir;

  @Test
  void shouldSaveExpenseAndReloadFromDisk() {
    Path file = tempDir.resolve("expenses.json");
    var repo1 = new JsonExpenseRepository(file);

    var id = UUID.randomUUID();
    var expense =
        new Expense(
            id,
            Money.of(new BigDecimal("12.50"), "GBP"),
            ExpenseCategory.FOOD,
            "Lunch",
            LocalDate.of(2026, 9, 7));

    repo1.save(expense);

    var repo2 = new JsonExpenseRepository(file);
    var found = repo2.findById(id);

    assertThat(found).isPresent();
    assertThat(found.get().id()).isEqualTo(id);
    assertThat(found.get().amount()).isEqualTo(Money.of(new BigDecimal("12.50"), "GBP"));
    assertThat(found.get().category()).isEqualTo(ExpenseCategory.FOOD);
    assertThat(found.get().description()).isEqualTo("Lunch");
    assertThat(found.get().date()).isEqualTo(LocalDate.of(2026, 9, 7));
  }

  @Test
  void shouldSaveMultipleExpensesAndPersistAcrossInstances() {
    Path file = tempDir.resolve("expenses.json");
    var repo1 = new JsonExpenseRepository(file);

    var id1 = UUID.randomUUID();
    var id2 = UUID.randomUUID();

    repo1.save(
        new Expense(
            id1,
            Money.of(new BigDecimal("12.50"), "GBP"),
            ExpenseCategory.FOOD,
            "Lunch",
            LocalDate.of(2026, 9, 7)));
    repo1.save(
        new Expense(
            id2,
            Money.of(new BigDecimal("4.75"), "GBP"),
            ExpenseCategory.TRANSPORT,
            "Bus",
            LocalDate.of(2026, 9, 6)));

    var repo2 = new JsonExpenseRepository(file);
    assertThat(repo2.findAll()).hasSize(2);
    assertThat(repo2.findById(id1)).isPresent();
    assertThat(repo2.findById(id2)).isPresent();
  }

  @Test
  void shouldRemoveExpenseAndVerifyDeletionPersisted() {
    Path file = tempDir.resolve("expenses.json");
    var repo1 = new JsonExpenseRepository(file);

    var id = UUID.randomUUID();
    repo1.save(
        new Expense(
            id,
            Money.of(new BigDecimal("12.50"), "GBP"),
            ExpenseCategory.FOOD,
            "Lunch",
            LocalDate.of(2026, 9, 7)));

    boolean deleted = repo1.deleteById(id);
    assertThat(deleted).isTrue();

    var repo2 = new JsonExpenseRepository(file);
    assertThat(repo2.findById(id)).isEmpty();
    assertThat(repo2.findAll()).isEmpty();
  }
}
