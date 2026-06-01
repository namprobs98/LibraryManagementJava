package com.librarymanagement.service;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.repository.BookRepository;
import com.librarymanagement.repository.BorrowRecordRepository;
import com.librarymanagement.repository.MemberRepository;

import java.time.LocalDate;
import java.util.List;

public class BorrowService {
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final StorageService storageService;

    public BorrowService(
            BookRepository bookRepository,
            MemberRepository memberRepository,
            BorrowRecordRepository borrowRecordRepository,
            StorageService storageService
    ) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.storageService = storageService;
    }

    public String borrowBook(String memberId, String bookId) {
        if (!memberRepository.existsById(memberId)) return "Member ID not found.";
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return "Book ID not found.";
        if (book.getBorrowed() >= book.getCopies()) return "All copies are currently borrowed.";
        book.setBorrowed(book.getBorrowed() + 1);
        bookRepository.save(book);
        String id = "BR" + (borrowRecordRepository.findAll().size() + 1);
        BorrowRecord record = new BorrowRecord(id, memberId, bookId, LocalDate.now().toString(), null);
        borrowRecordRepository.save(record);
        storageService.persistCurrentIfNeeded();
        return "Borrowed successfully.";
    }

    public String returnBook(String memberId, String bookId) {
        if (!memberRepository.existsById(memberId)) return "Member ID not found.";
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return "Book ID not found.";
        if (book.getBorrowed() <= 0) return "No copies are currently borrowed.";

        List<BorrowRecord> all = borrowRecordRepository.findAll();
        BorrowRecord open = null;
        for (BorrowRecord record : all) {
            if (record.getMemberId().equals(memberId) && record.getBookId().equals(bookId) &&
                (record.getReturnDate() == null || record.getReturnDate().isBlank())) {
                open = record;
                break;
            }
        }
        if (open == null) return "No active borrow record found.";

        open.setReturnDate(LocalDate.now().toString());
        book.setBorrowed(book.getBorrowed() - 1);
        bookRepository.save(book);
        borrowRecordRepository.replaceAll(all);
        storageService.persistCurrentIfNeeded();
        return "Returned successfully.";
    }

    public List<BorrowRecord> getRecords() {
        return borrowRecordRepository.findAll();
    }
}