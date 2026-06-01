package com.librarymanagement.repository;

import com.librarymanagement.entity.BorrowRecord;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository {
    void save(BorrowRecord record);
    void save(BorrowRecord record, Connection conn);
    Optional<BorrowRecord> findById(String id);
    List<BorrowRecord> findAll();
    List<BorrowRecord> findAll(Connection conn);
    PagedResult<BorrowRecord> findAll(PageRequest pageRequest);
    long count();
    void deleteById(String id);
    boolean existsById(String id);
    void replaceAll(List<BorrowRecord> records);
    void replaceAll(List<BorrowRecord> records, Connection conn);
}