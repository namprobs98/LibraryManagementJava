package com.librarymanagement.repository;

import com.librarymanagement.entity.BorrowRecord;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository {
    void save(BorrowRecord record);
    Optional<BorrowRecord> findById(String id);
    List<BorrowRecord> findAll();
    PagedResult<BorrowRecord> findAll(PageRequest pageRequest);
    long count();
    void deleteById(String id);
    boolean existsById(String id);
    void replaceAll(List<BorrowRecord> records);
}