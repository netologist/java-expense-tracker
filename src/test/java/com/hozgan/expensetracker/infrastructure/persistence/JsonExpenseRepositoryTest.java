package com.hozgan.expensetracker.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpenseCategory;
import com.hozgan.expensetracker.domain.expense.ExpensePersistenceException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonExpenseRepositoryTest {

  @TempDir Path tempDir;

  private Path jsonFile;
  private JsonExpenseRepository repository;

  @BeforeEach
  void setUp() {
    jsonFile = tempDir.resolve("expenses.json");
    repository = new JsonExpenseRepository(jsonFile);
  }

  @Test
  void shouldReturnEmptyListWhenFileDoesNotExist() {
    assertThat(repository.findAll()).isEmpty();
  }

  @Test
  void shouldReturnEmptyListWhenFileIsEmpty() throws IOException {
    Files.createFile(jsonFile);
    assertThat(repository.findAll()).isEmpty();
  }

  @Test
  void shouldSaveAndFindExpense() {
    var id = UUID.randomUUID();
    var expense =
        new Expense(
            id,
            Money.of(new BigDecimal("12.50"), "GBP"),
            ExpenseCategory.FOOD,
            "Lunch",
            LocalDate.of(2026, 9, 7));

    repository.save(expense);

    var found = repository.findById(id);
    assertThat(found).isPresent();
    assertThat(found.get().id()).isEqualTo(id);
    assertThat(found.get().amount()).isEqualTo(Money.of(new BigDecimal("12.50"), "GBP"));
    assertThat(found.get().category()).isEqualTo(ExpenseCategory.FOOD);
    assertThat(found.get().description()).isEqualTo("Lunch");
    assertThat(found.get().date()).isEqualTo(LocalDate.of(2026, 9, 7));

    var all = repository.findAll();
    assertThat(all).hasSize(1);
    assertThat(all.getFirst().id()).isEqualTo(id);
  }

  @Test
  void shouldPersistDataAcrossInstances() {
    var expense1 =
        new Expense(
            UUID.randomUUID(),
            Money.of(new BigDecimal("12.50"), "GBP"),
            ExpenseCategory.FOOD,
            "Lunch",
            LocalDate.of(2026, 9, 7));
    var expense2 =
        new Expense(
            UUID.randomUUID(),
            Money.of(new BigDecimal("4.75"), "GBP"),
            ExpenseCategory.TRANSPORT,
            "Bus",
            LocalDate.of(2026, 9, 7));

    repository.save(expense1);
    repository.save(expense2);

    var newRepo = new JsonExpenseRepository(jsonFile);
    assertThat(newRepo.findAll()).hasSize(2);
    assertThat(newRepo.findById(expense1.id())).isPresent();
    assertThat(newRepo.findById(expense2.id())).isPresent();
  }

  @Test
  void shouldUpdateExistingExpenseWhenIdMatches() {
    var id = UUID.randomUUID();
    var expense =
        new Expense(
            id,
            Money.of(new BigDecimal("10.00"), "GBP"),
            ExpenseCategory.FOOD,
            "Lunch",
            LocalDate.of(2026, 9, 7));
    repository.save(expense);

    var updated =
        new Expense(
            id,
            Money.of(new BigDecimal("15.00"), "GBP"),
            ExpenseCategory.FOOD,
            "Big Lunch",
            LocalDate.of(2026, 9, 7));
    repository.save(updated);

    assertThat(repository.findAll()).hasSize(1);
    assertThat(repository.findById(id)).contains(updated);
  }

  @Test
  void shouldDeleteExpenseById() {
    var id = UUID.randomUUID();
    var expense =
        new Expense(
            id,
            Money.of(new BigDecimal("12.50"), "GBP"),
            ExpenseCategory.FOOD,
            "Lunch",
            LocalDate.of(2026, 9, 7));
    repository.save(expense);

    boolean removed = repository.deleteById(id);
    assertThat(removed).isTrue();
    assertThat(repository.findById(id)).isEmpty();
    assertThat(repository.findAll()).isEmpty();

    var newRepo = new JsonExpenseRepository(jsonFile);
    assertThat(newRepo.findById(id)).isEmpty();
  }

  @Test
  void shouldReturnFalseWhenDeletingNonExistentExpense() {
    boolean removed = repository.deleteById(UUID.randomUUID());
    assertThat(removed).isFalse();
  }

  @Test
  void shouldThrowExpensePersistenceExceptionOnCorruptedJson() throws IOException {
    Files.writeString(jsonFile, "{ corrupted json }");

    assertThatThrownBy(() -> repository.findAll())
        .isInstanceOf(ExpensePersistenceException.class)
        .hasMessageContaining("Failed to read expenses");
  }

  @Test
  void shouldCreateParentDirectoriesIfTheyDoNotExist() {
    Path nested = tempDir.resolve("sub").resolve("dir").resolve("expenses.json");
    var nestedRepo = new JsonExpenseRepository(nested);

    var expense =
        new Expense(
            UUID.randomUUID(),
            Money.of(new BigDecimal("1.00"), "USD"),
            ExpenseCategory.OTHER,
            "Test",
            LocalDate.of(2026, 9, 7));

    nestedRepo.save(expense);
    assertThat(Files.exists(nested)).isTrue();
    assertThat(nestedRepo.findAll()).hasSize(1);
  }
}
