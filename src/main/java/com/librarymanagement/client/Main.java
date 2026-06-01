package com.librarymanagement.client;

import com.librarymanagement.entity.Book;
import com.librarymanagement.entity.BorrowRecord;
import com.librarymanagement.entity.Member;
import com.librarymanagement.repository.*;
import com.librarymanagement.service.*;

import java.util.List;
import java.util.Scanner;

public class Main {
    private final BookService bookService;
    private final MemberService memberService;
    private final BorrowService borrowService;
    private final StorageService storageService;
    private final Scanner scanner;

    public Main() {
        BookRepository bookRepository = new BookRepositoryImpl();
        MemberRepository memberRepository = new MemberRepositoryImpl();
        BorrowRecordRepository borrowRecordRepository = new BorrowRecordRepositoryImpl();

        storageService = new StorageService(bookRepository, memberRepository, borrowRecordRepository);
        bookService = new BookService(bookRepository, storageService);
        memberService = new MemberService(memberRepository, storageService);
        borrowService = new BorrowService(bookRepository, memberRepository, borrowRecordRepository, storageService);
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        DatabaseConnection.initializeDatabase();
        Main app = new Main();
        app.run();
    }

    public void run() {
        BootstrapService bootstrapService = new BootstrapService(
                new BookRepositoryImpl(),
                new MemberRepositoryImpl()
        );
        bootstrapService.seedIfEmpty();

        int choice = 0;
        while (choice != 5) {
            System.out.println("\n=== Library Management (" + storageService.getCurrentMode() + ") ===");
            System.out.println("1. Manage books");
            System.out.println("2. Manage members");
            System.out.println("3. Manage borrowing");
            System.out.println("4. Change storage format");
            System.out.println("5. Exit");
            choice = readInt(1, 5);

            switch (choice) {
                case 1 -> manageBooks();
                case 2 -> manageMembers();
                case 3 -> manageBorrowing();
                case 4 -> chooseStorage();
                case 5 -> System.out.println("Goodbye!");
            }
        }
    }

    private void manageBooks() {
        System.out.println("\n--- Manage Books ---");
        System.out.println("1. Add book");
        System.out.println("2. List all books");
        System.out.println("3. Update book");
        System.out.println("4. Delete book");
        System.out.println("5. Back");
        int choice = readInt(1, 5);

        switch (choice) {
            case 1 -> addBook();
            case 2 -> listBooks();
            case 3 -> updateBook();
            case 4 -> deleteBook();
        }
    }

    private void addBook() {
        System.out.print("Book ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("Genre: ");
        String genre = scanner.nextLine().trim();
        System.out.print("Copies: ");
        int copies = readInt();

        Book book = new Book(id, title, author, genre, copies);
        boolean success = bookService.addBook(book);
        System.out.println(success ? "Book added successfully!" : "Book ID already exists!");
    }

    private void listBooks() {
        List<Book> books = bookService.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.println("\n--- Books ---");
        for (Book b : books) {
            System.out.println(b);
        }
    }

    private void updateBook() {
        System.out.print("Book ID to update: ");
        String id = scanner.nextLine().trim();
        System.out.print("New Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("New Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("New Genre: ");
        String genre = scanner.nextLine().trim();
        System.out.print("New Copies: ");
        int copies = readInt();

        boolean success = bookService.updateBook(id, title, author, genre, copies);
        System.out.println(success ? "Book updated successfully!" : "Book not found!");
    }

    private void deleteBook() {
        System.out.print("Book ID to delete: ");
        String id = scanner.nextLine().trim();
        boolean success = bookService.deleteBook(id);
        System.out.println(success ? "Book deleted successfully!" : "Book not found!");
    }

    private void manageMembers() {
        System.out.println("\n--- Manage Members ---");
        System.out.println("1. Add member");
        System.out.println("2. List all members");
        System.out.println("3. Update member");
        System.out.println("4. Delete member");
        System.out.println("5. Back");
        int choice = readInt(1, 5);

        switch (choice) {
            case 1 -> addMember();
            case 2 -> listMembers();
            case 3 -> updateMember();
            case 4 -> deleteMember();
        }
    }

    private void addMember() {
        System.out.print("Member ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();

        Member member = new Member(id, name, email, phone);
        boolean success = memberService.addMember(member);
        System.out.println(success ? "Member added successfully!" : "Member ID already exists!");
    }

    private void listMembers() {
        List<Member> members = memberService.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }
        System.out.println("\n--- Members ---");
        for (Member m : members) {
            System.out.println(m);
        }
    }

    private void updateMember() {
        System.out.print("Member ID to update: ");
        String id = scanner.nextLine().trim();
        System.out.print("New Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("New Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("New Phone: ");
        String phone = scanner.nextLine().trim();

        boolean success = memberService.updateMember(id, name, email, phone);
        System.out.println(success ? "Member updated successfully!" : "Member not found!");
    }

    private void deleteMember() {
        System.out.print("Member ID to delete: ");
        String id = scanner.nextLine().trim();
        boolean success = memberService.deleteMember(id);
        System.out.println(success ? "Member deleted successfully!" : "Member not found!");
    }

    private void manageBorrowing() {
        System.out.println("\n--- Manage Borrowing ---");
        System.out.println("1. Borrow book");
        System.out.println("2. Return book");
        System.out.println("3. List all borrow records");
        System.out.println("4. Back");
        int choice = readInt(1, 4);

        switch (choice) {
            case 1 -> borrowBook();
            case 2 -> returnBook();
            case 3 -> listBorrowRecords();
        }
    }

    private void borrowBook() {
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();
        System.out.print("Book ID: ");
        String bookId = scanner.nextLine().trim();

        String result = borrowService.borrowBook(memberId, bookId);
        System.out.println(result);
    }

    private void returnBook() {
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();
        System.out.print("Book ID: ");
        String bookId = scanner.nextLine().trim();

        String result = borrowService.returnBook(memberId, bookId);
        System.out.println(result);
    }

    private void listBorrowRecords() {
        List<BorrowRecord> records = borrowService.getRecords();
        if (records.isEmpty()) {
            System.out.println("No borrow records found.");
            return;
        }
        System.out.println("\n--- Borrow Records ---");
        for (BorrowRecord r : records) {
            System.out.println(r);
        }
    }

    private void chooseStorage() {
        System.out.println("\n--- Change Storage Format ---");
        System.out.println("1. DATABASE (PostgreSQL)");
        System.out.println("2. MEMORY");
        System.out.println("3. TXT");
        System.out.println("4. EXCEL");
        System.out.println("5. Back");
        int choice = readInt(1, 5);

        StorageMode mode = switch (choice) {
            case 1 -> StorageMode.DATABASE;
            case 2 -> StorageMode.MEMORY;
            case 3 -> StorageMode.TXT;
            case 4 -> StorageMode.EXCEL;
            default -> null;
        };

        if (mode != null) {
            storageService.switchMode(mode);
            System.out.println("Storage mode changed to: " + mode);
        }
    }

    private int readInt() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
    }

    private int readInt(int min, int max) {
        while (true) {
            int value = readInt();
            if (value >= min && value <= max) {
                return value;
            }
            System.out.print("Please enter between " + min + " and " + max + ": ");
        }
    }
}