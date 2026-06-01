package com.librarymanagement.entity;

public class BorrowRecord {
    private String id;
    private String memberId;
    private String bookId;
    private String borrowDate;
    private String returnDate;

    public BorrowRecord() {
    }

    public BorrowRecord(String id, String memberId, String bookId, String borrowDate, String returnDate) {
        this.id = id;
        this.memberId = memberId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    @Override
    public String toString() {
        return String.format("BorrowRecord[id=%s, memberId=%s, bookId=%s, borrowDate=%s, returnDate=%s]",
                id, memberId, bookId, borrowDate, returnDate);
    }
}