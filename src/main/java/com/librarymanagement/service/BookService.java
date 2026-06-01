package com.librarymanagement.service;

import com.librarymanagement.entity.Book;
import com.librarymanagement.repository.BookRepository;

import java.util.List;
import java.util.Optional;

public class BookService {
    private final BookRepository bookRepository;
    private final StorageService storageService;

    public BookService(BookRepository bookRepository, StorageService storageService) {
        this.bookRepository = bookRepository;
        this.storageService = storageService;
    }

    public boolean addBook(Book book) {
        if (bookRepository.existsById(book.getId())) return false;
        bookRepository.save(book);
        storageService.persistCurrentIfNeeded();
        return true;
    }

    public Optional<Book> getBookById(String id) {
        return bookRepository.findById(id);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public boolean updateBook(String id, String title, String author, String genre, int copies) {
        Optional<Book> found = bookRepository.findById(id);
        if (found.isEmpty()) return false;
        Book book = found.get();
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setCopies(copies);
        bookRepository.save(book);
        storageService.persistCurrentIfNeeded();
        return true;
    }

    public boolean deleteBook(String id) {
        if (!bookRepository.existsById(id)) return false;
        bookRepository.deleteById(id);
        storageService.persistCurrentIfNeeded();
        return true;
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return bookRepository.search(query.trim());
    }
}