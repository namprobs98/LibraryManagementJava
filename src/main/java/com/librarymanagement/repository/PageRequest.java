package com.librarymanagement.repository;

public class PageRequest {
    private final int page;
    private final int size;
    private final String sortBy;
    private final boolean ascending;
    private final String searchQuery;
    private final String genre;
    private final String author;

    public PageRequest(int page, int size) {
        this(page, size, "id", true, null, null, null);
    }

    public PageRequest(int page, int size, String sortBy, boolean ascending) {
        this(page, size, sortBy, ascending, null, null, null);
    }

    public PageRequest(int page, int size, String sortBy, boolean ascending,
                       String searchQuery, String genre, String author) {
        this.page = Math.max(0, page);
        this.size = Math.max(1, Math.min(size, 1000)); // Max 1000 per page
        this.sortBy = sortBy;
        this.ascending = ascending;
        this.searchQuery = searchQuery;
        this.genre = genre;
        this.author = author;
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
    public String getSortBy() { return sortBy; }
    public boolean isAscending() { return ascending; }
    public int getOffset() { return page * size; }

    public String getSearchQuery() { return searchQuery; }
    public String getGenre() { return genre; }
    public String getAuthor() { return author; }

    public boolean hasFilter() {
        return (searchQuery != null && !searchQuery.isBlank()) ||
               (genre != null && !genre.isBlank()) ||
               (author != null && !author.isBlank());
    }
}