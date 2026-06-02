package com.librarymanagement.repository;

import com.librarymanagement.entity.BorrowRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BorrowRecordRepositoryImpl implements BorrowRecordRepository {
    @Override
    public void save(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records (id, member_id, book_id, borrow_date, return_date) VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET member_id = EXCLUDED.member_id, book_id = EXCLUDED.book_id, " +
                     "borrow_date = EXCLUDED.borrow_date, return_date = EXCLUDED.return_date";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, record.getId());
            stmt.setString(2, record.getMemberId());
            stmt.setString(3, record.getBookId());
            stmt.setString(4, record.getBorrowDate());
            stmt.setString(5, record.getReturnDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving borrow record: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(BorrowRecord record, Connection conn) {
        String sql = "INSERT INTO borrow_records (id, member_id, book_id, borrow_date, return_date) VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET member_id = EXCLUDED.member_id, book_id = EXCLUDED.book_id, " +
                     "borrow_date = EXCLUDED.borrow_date, return_date = EXCLUDED.return_date";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, record.getId());
            stmt.setString(2, record.getMemberId());
            stmt.setString(3, record.getBookId());
            stmt.setString(4, record.getBorrowDate());
            stmt.setString(5, record.getReturnDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving borrow record: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<BorrowRecord> findById(String id) {
        String sql = "SELECT * FROM borrow_records WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding borrow record: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<BorrowRecord> findAll() {
        return findAll(new PageRequest(0, 1000)).getData();
    }

    @Override
    public List<BorrowRecord> findAll(Connection conn) {
        return findAll(new PageRequest(0, 1000), conn).getData();
    }

    @Override
    public PagedResult<BorrowRecord> findAll(PageRequest pageRequest) {
        List<BorrowRecord> records = new ArrayList<>();
        String order = pageRequest.isAscending() ? "ASC" : "DESC";
        String sql = """
            SELECT br.*, b.title as book_title, m.name as member_name
            FROM borrow_records br
            LEFT JOIN books b ON br.book_id = b.id
            LEFT JOIN members m ON br.member_id = m.id
            ORDER BY br.""" + pageRequest.getSortBy() + " " + order +
                     " LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pageRequest.getSize());
            stmt.setInt(2, pageRequest.getOffset());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding borrow records: " + e.getMessage(), e);
        }

        long total = count();
        return new PagedResult<>(records, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    public PagedResult<BorrowRecord> findAll(PageRequest pageRequest, Connection conn) {
        List<BorrowRecord> records = new ArrayList<>();
        String order = pageRequest.isAscending() ? "ASC" : "DESC";
        String sql = """
            SELECT br.*, b.title as book_title, m.name as member_name
            FROM borrow_records br
            LEFT JOIN books b ON br.book_id = b.id
            LEFT JOIN members m ON br.member_id = m.id
            ORDER BY br.""" + pageRequest.getSortBy() + " " + order +
                     " LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pageRequest.getSize());
            stmt.setInt(2, pageRequest.getOffset());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding borrow records: " + e.getMessage(), e);
        }

        long total = count();
        return new PagedResult<>(records, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM borrow_records";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting borrow records: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM borrow_records WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting borrow record: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM borrow_records WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking borrow record existence: " + e.getMessage(), e);
        }
    }

    @Override
    public void replaceAll(List<BorrowRecord> records) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM borrow_records");
            for (BorrowRecord record : records) {
                save(record);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error replacing all borrow records: " + e.getMessage(), e);
        }
    }

    @Override
    public void replaceAll(List<BorrowRecord> records, Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM borrow_records");
            for (BorrowRecord record : records) {
                save(record, conn);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error replacing all borrow records: " + e.getMessage(), e);
        }
    }

    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord(
                rs.getString("id"),
                rs.getString("member_id"),
                rs.getString("book_id"),
                rs.getString("borrow_date"),
                rs.getString("return_date")
        );
        record.setDueDate(rs.getString("due_date"));
        // Try to get book title and member name from JOIN results if available
        try {
            record.setBookTitle(rs.getString("book_title"));
        } catch (SQLException e) {
            // Column not in resultset - ignore
        }
        try {
            record.setMemberName(rs.getString("member_name"));
        } catch (SQLException e) {
            // Column not in resultset - ignore
        }
        return record;
    }
}