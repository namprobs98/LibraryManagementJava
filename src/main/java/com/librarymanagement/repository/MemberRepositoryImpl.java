package com.librarymanagement.repository;

import com.librarymanagement.entity.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberRepositoryImpl implements MemberRepository {
    @Override
    public void save(Member member) {
        String sql = "INSERT INTO members (id, name, email, phone, joined_date) VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, email = EXCLUDED.email, phone = EXCLUDED.phone, joined_date = EXCLUDED.joined_date";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getId());
            stmt.setString(2, member.getName());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getPhone());
            stmt.setString(5, member.getJoinedDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving member: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Member member, Connection conn) {
        String sql = "INSERT INTO members (id, name, email, phone, joined_date) VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, email = EXCLUDED.email, phone = EXCLUDED.phone, joined_date = EXCLUDED.joined_date";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getId());
            stmt.setString(2, member.getName());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getPhone());
            stmt.setString(5, member.getJoinedDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving member: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Member> findById(String id) {
        String sql = "SELECT * FROM members WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding member: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Member> findAll() {
        return findAll(new PageRequest(0, 1000)).getData();
    }

    @Override
    public PagedResult<Member> findAll(PageRequest pageRequest) {
        List<Member> members = new ArrayList<>();
        String order = pageRequest.isAscending() ? "ASC" : "DESC";
        String sql = "SELECT * FROM members ORDER BY " + pageRequest.getSortBy() + " " + order +
                     " LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pageRequest.getSize());
            stmt.setInt(2, pageRequest.getOffset());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding members: " + e.getMessage(), e);
        }

        long total = count();
        return new PagedResult<>(members, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM members";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting members: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting member: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM members WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking member existence: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id, Connection conn) {
        String sql = "SELECT 1 FROM members WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking member existence: " + e.getMessage(), e);
        }
    }

    @Override
    public void replaceAll(List<Member> members) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM members");
            for (Member member : members) {
                save(member);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error replacing all members: " + e.getMessage(), e);
        }
    }

    @Override
    public void replaceAll(List<Member> members, Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM members");
            for (Member member : members) {
                save(member, conn);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error replacing all members: " + e.getMessage(), e);
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        Member member = new Member(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone")
        );
        member.setJoinedDate(rs.getString("joined_date"));
        return member;
    }
}