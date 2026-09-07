package com.hozgan.expensetracker.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hozgan.expensetracker.domain.expense.Expense;
import com.hozgan.expensetracker.domain.expense.ExpensePersistenceException;
import com.hozgan.expensetracker.domain.expense.ExpenseRepository;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class JsonExpenseRepository implements ExpenseRepository {

  private final Path filePath;
  private final ObjectMapper objectMapper;

  public JsonExpenseRepository(Path filePath) {
    this(filePath, createDefaultObjectMapper());
  }

  public JsonExpenseRepository(Path filePath, ObjectMapper objectMapper) {
    this.filePath = Objects.requireNonNull(filePath, "filePath must not be null");
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
  }

  private static ObjectMapper createDefaultObjectMapper() {
    var mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper;
  }

  @Override
  public synchronized void save(Expense expense) {
    Objects.requireNonNull(expense, "expense must not be null");
    List<ExpenseDto> dtos = readDtos();
    dtos.removeIf(dto -> dto.id().equals(expense.id()));
    dtos.add(ExpenseDto.fromDomain(expense));
    writeDtos(dtos);
  }

  @Override
  public synchronized List<Expense> findAll() {
    List<ExpenseDto> dtos = readDtos();
    return dtos.stream().map(ExpenseDto::toDomain).toList();
  }

  @Override
  public synchronized boolean deleteById(UUID id) {
    Objects.requireNonNull(id, "id must not be null");
    List<ExpenseDto> dtos = readDtos();
    boolean removed = dtos.removeIf(dto -> dto.id().equals(id));
    if (removed) {
      writeDtos(dtos);
    }
    return removed;
  }

  @Override
  public synchronized Optional<Expense> findById(UUID id) {
    Objects.requireNonNull(id, "id must not be null");
    return readDtos().stream()
        .filter(dto -> dto.id().equals(id))
        .findFirst()
        .map(ExpenseDto::toDomain);
  }

  private List<ExpenseDto> readDtos() {
    if (!Files.exists(filePath)) {
      return new ArrayList<>();
    }
    try {
      if (Files.size(filePath) == 0) {
        return new ArrayList<>();
      }
      List<ExpenseDto> dtos =
          objectMapper.readValue(filePath.toFile(), new TypeReference<List<ExpenseDto>>() {});
      return dtos != null ? new ArrayList<>(dtos) : new ArrayList<>();
    } catch (IOException e) {
      throw new ExpensePersistenceException("Failed to read expenses from " + filePath, e);
    }
  }

  private void writeDtos(List<ExpenseDto> dtos) {
    try {
      Path parent = filePath.getParent();
      if (parent != null && !Files.exists(parent)) {
        Files.createDirectories(parent);
      }
      Path tempFile =
          parent != null
              ? Files.createTempFile(parent, "expenses", ".tmp")
              : Files.createTempFile("expenses", ".tmp");
      try {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), dtos);
        try {
          Files.move(
              tempFile,
              filePath,
              StandardCopyOption.ATOMIC_MOVE,
              StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
          Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
      } finally {
        Files.deleteIfExists(tempFile);
      }
    } catch (IOException e) {
      throw new ExpensePersistenceException("Failed to write expenses to " + filePath, e);
    }
  }
}
