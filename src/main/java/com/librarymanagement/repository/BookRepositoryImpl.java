package com.librarymanagement.repository;

import com.librarymanagement.entity.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookRepositoryImpl implements BookRepository {
    @Override
    public void save(Book book) {
        String sql = "INSERT INTO books (id, title, author, genre, copies, borrowed) VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, author = EXCLUDED.author, " +
                     "genre = EXCLUDED.genre, copies = EXCLUDED.copies, borrowed = EXCLUDED.borrowed";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getId());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getGenre());
            stmt.setInt(5, book.getCopies());
            stmt.setInt(6, book.getBorrowed());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving book: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Book book, Connection conn) {
        String sql = "INSERT INTO books (id, title, author, genre, copies, borrowed) VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, author = EXCLUDED.author, " +
                     "genre = EXCLUDED.genre, copies = EXCLUDED.copies, borrowed = EXCLUDED.borrowed";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getId());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getGenre());
            stmt.setInt(5, book.getCopies());
            stmt.setInt(6, book.getBorrowed());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving book: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Book> findById(String id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding book: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Book> findById(String id, Connection conn) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding book: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Book> findAll() {
        return findAll(new PageRequest(0, 1000)).getData();
    }

    @Override
    public PagedResult<Book> findAll(PageRequest pageRequest) {
        List<Book> books = new ArrayList<>();

        // Enable unaccent extension if not exists (for Vietnamese search support)
        ensureUnaccentEnabled();

        // Build WHERE clause and parameters based on filters
        StringBuilder whereClause = new StringBuilder();
        List<Object> params = buildFilterParams(pageRequest, whereClause);

        String order = pageRequest.isAscending() ? "ASC" : "DESC";
        String sql = "SELECT * FROM books" + whereClause + " ORDER BY " + pageRequest.getSortBy() + " " + order +
                     " LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set filter params
            setFilterParams(stmt, params);
            // Set pagination params
            stmt.setInt(params.size() + 1, pageRequest.getSize());
            stmt.setInt(params.size() + 2, pageRequest.getOffset());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding books: " + e.getMessage(), e);
        }

        long total = count(pageRequest);
        return new PagedResult<>(books, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    @Override
    public long count(PageRequest pageRequest) {
        // Enable unaccent extension if not exists (for Vietnamese search support)
        ensureUnaccentEnabled();

        StringBuilder whereClause = new StringBuilder();
        List<Object> params = buildFilterParams(pageRequest, whereClause);

        String sql = "SELECT COUNT(*) FROM books" + whereClause;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setFilterParams(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting books: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public long count() {
        return count(new PageRequest(0, Integer.MAX_VALUE));
    }

    /**
     * Build filter WHERE clause and collect parameters
     */
    private List<Object> buildFilterParams(PageRequest pageRequest, StringBuilder whereClause) {
        List<Object> params = new ArrayList<>();

        String searchQuery = pageRequest.getSearchQuery();
        String genre = pageRequest.getGenre();
        String author = pageRequest.getAuthor();

        boolean hasFilter = (searchQuery != null && !searchQuery.isBlank()) ||
                           (genre != null && !genre.isBlank()) ||
                           (author != null && !author.isBlank());

        if (hasFilter) {
            whereClause.append(" WHERE ");
            List<String> conditions = new ArrayList<>();

            if (searchQuery != null && !searchQuery.isBlank()) {
                conditions.add("(unaccent(LOWER(title)) LIKE unaccent(LOWER(?)) OR " +
                             "unaccent(LOWER(author)) LIKE unaccent(LOWER(?)) OR " +
                             "unaccent(LOWER(genre)) LIKE unaccent(LOWER(?)))");
                String pattern = "%" + searchQuery.toLowerCase() + "%";
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }

            if (genre != null && !genre.isBlank()) {
                conditions.add("unaccent(LOWER(genre)) = unaccent(LOWER(?))");
                params.add(genre);
            }

            if (author != null && !author.isBlank()) {
                conditions.add("unaccent(LOWER(author)) = unaccent(LOWER(?))");
                params.add(author);
            }

            whereClause.append(String.join(" AND ", conditions));
        }

        return params;
    }

    /**
     * Set filter parameters to PreparedStatement
     */
    private void setFilterParams(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof String) {
                stmt.setString(i + 1, (String) param);
            } else if (param instanceof Integer) {
                stmt.setInt(i + 1, (Integer) param);
            } else if (param instanceof Long) {
                stmt.setLong(i + 1, (Long) param);
            }
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting book: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking book existence: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id, Connection conn) {
        String sql = "SELECT 1 FROM books WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking book existence: " + e.getMessage(), e);
        }
    }

    @Override
    public void replaceAll(List<Book> books) {
        String deleteSql = "DELETE FROM books";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(deleteSql);
            }

            for (Book book : books) {
                String insertSql = "INSERT INTO books(id,title,author,genre,copies,borrowed) " +
                                    "VALUES(?,?,?,?,?,?)";

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, book.getId());
                    ps.setString(2, book.getTitle());
                    ps.setString(3, book.getAuthor());
                    ps.setString(4, book.getGenre());
                    ps.setInt(5, book.getCopies());
                    ps.setInt(6, book.getBorrowed());
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Ensure unaccent extension is enabled for Vietnamese search support
     */
    private void ensureUnaccentEnabled() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE EXTENSION IF NOT EXISTS unaccent");
        } catch (SQLException e) {
            // Extension might already exist or no permission, continue anyway
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("genre"),
                rs.getInt("copies")
        );
        book.setBorrowed(rs.getInt("borrowed"));
        return book;
    }

    @Override
    public List<Book> search(String query) {
        List<Book> books = new ArrayList<>();

        // Enable unaccent extension if not exists
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE EXTENSION IF NOT EXISTS unaccent");
        } catch (SQLException e) {
            // Extension might already exist or no permission, continue anyway
        }

        // Search with unaccent for Vietnamese support
        // The query is matched in sequence but can skip characters
        // e.g., "De men" matches "Dế mèn phiêu lưu lý"
        String sql = """
            SELECT * FROM books
            WHERE unaccent(LOWER(title)) LIKE unaccent(LOWER(?))
               OR unaccent(LOWER(author)) LIKE unaccent(LOWER(?))
               OR unaccent(LOWER(genre)) LIKE unaccent(LOWER(?))
            ORDER BY title
            LIMIT 100
            """;

        // Convert query to LIKE pattern: "De men" -> "%d%e%m%e%n%"
        // Each character in sequence must appear, but can have anything in between
        StringBuilder pattern = new StringBuilder("%");
        for (char c : query.toLowerCase().toCharArray()) {
            pattern.append(c).append("%");
        }
        String likeQuery = pattern.toString();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, likeQuery);
            stmt.setString(2, likeQuery);
            stmt.setString(3, likeQuery);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching books: " + e.getMessage(), e);
        }

        return books;
    }
}