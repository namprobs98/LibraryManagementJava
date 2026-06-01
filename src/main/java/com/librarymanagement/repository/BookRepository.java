package com.librarymanagement.repository;

import com.librarymanagement.entity.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    void save(Book book);
    Optional<Book> findById(String id);
    List<Book> findAll();
    void deleteById(String id);
    boolean existsById(String id);
    void replaceAll(List<Book> books);
}