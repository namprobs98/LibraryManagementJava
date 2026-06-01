package com.librarymanagement.repository;

import java.util.List;

public class PagedResult<T> {
    private final List<T> data;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public PagedResult(List<T> data, int page, int size, long totalElements) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }

    public List<T> getData() { return data; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean hasNext() { return page < totalPages - 1; }
    public boolean hasPrevious() { return page > 0; }

    @Override
    public String toString() {
        return String.format("Page %d/%d (Total: %d items)", page + 1, totalPages, totalElements);
    }
}