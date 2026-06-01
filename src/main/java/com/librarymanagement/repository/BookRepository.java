package com.librarymanagement.repository;

import com.librarymanagement.entity.Book;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    void save(Book book);
    void save(Book book, Connection conn);
    Optional<Book> findById(String id);
    Optional<Book> findById(String id, Connection conn);
    List<Book> findAll();
    PagedResult<Book> findAll(PageRequest pageRequest);
    long count();
    void deleteById(String id);
    boolean existsById(String id);
    boolean existsById(String id, Connection conn);
    void replaceAll(List<Book> books);
    List<Book> search(String query);
}