package com.librarymanagement.repository;

public class PageRequest {
    private final int page;
    private final int size;
    private final String sortBy;
    private final boolean ascending;

    public PageRequest(int page, int size) {
        this(page, size, "id", true);
    }

    public PageRequest(int page, int size, String sortBy, boolean ascending) {
        this.page = Math.max(0, page);
        this.size = Math.max(1, Math.min(size, 1000)); // Max 1000 per page
        this.sortBy = sortBy;
        this.ascending = ascending;
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
    public String getSortBy() { return sortBy; }
    public boolean isAscending() { return ascending; }
    public int getOffset() { return page * size; }
}