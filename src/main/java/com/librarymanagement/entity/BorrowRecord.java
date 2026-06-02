package com.librarymanagement.entity;

public class BorrowRecord {
    private String id;
    private String memberId;
    private String bookId;
    private String borrowDate;
    private String returnDate;
    private String dueDate;

    // For display purposes - populated when fetching records
    private String bookTitle;
    private String memberName;

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
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public boolean isReturned() {
        return returnDate != null && !returnDate.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("BorrowRecord[id=%s, memberId=%s, bookId=%s, borrowDate=%s, returnDate=%s]",
                id, memberId, bookId, borrowDate, returnDate);
    }
}