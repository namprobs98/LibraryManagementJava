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
    public List<Book> findAll() {
        return findAll(new PageRequest(0, 1000)).getData();
    }

    @Override
    public PagedResult<Book> findAll(PageRequest pageRequest) {
        List<Book> books = new ArrayList<>();
        String order = pageRequest.isAscending() ? "ASC" : "DESC";
        String sql = "SELECT * FROM books ORDER BY " + pageRequest.getSortBy() + " " + order +
                     " LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pageRequest.getSize());
            stmt.setInt(2, pageRequest.getOffset());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding books: " + e.getMessage(), e);
        }

        long total = count();
        return new PagedResult<>(books, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting books: " + e.getMessage(), e);
        }
        return 0;
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
public void replaceAll(List<Book> books) {

    String deleteSql = "DELETE FROM books";

    try (Connection conn = DatabaseConnection.getConnection()) {

        conn.setAutoCommit(false);

        try {

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(deleteSql);
            }

            for (Book book : books) {

                String insertSql =
                        "INSERT INTO books(id,title,author,genre,copies,borrowed) " +
                        "VALUES(?,?,?,?,?,?)";

                try (PreparedStatement ps =
                             conn.prepareStatement(insertSql)) {

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

            conn.rollback();
            throw e;
        }

    } catch (Exception e) {
        throw new RuntimeException(e);
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