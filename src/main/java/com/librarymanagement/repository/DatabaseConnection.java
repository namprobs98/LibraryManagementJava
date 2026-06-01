package com.librarymanagement.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/LibraryData";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create books table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    id VARCHAR(255) PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    author VARCHAR(255),
                    genre VARCHAR(255),
                    copies INTEGER DEFAULT 0,
                    borrowed INTEGER DEFAULT 0
                )
            """);

            // Create members table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS members (
                    id VARCHAR(255) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    email VARCHAR(255),
                    phone VARCHAR(255)
                )
            """);

            // Create borrow_records table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS borrow_records (
                    id VARCHAR(255) PRIMARY KEY,
                    member_id VARCHAR(255),
                    book_id VARCHAR(255),
                    borrow_date VARCHAR(255),
                    return_date VARCHAR(255)
                )
            """);

            System.out.println("Database tables initialized successfully!");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Rollback một connection mà không throw exception.
     * Dùng trong catch block khi đã có exception khác cần xử lý.
     */
    public static void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                System.err.println("Error during rollback: " + e.getMessage());
            }
        }
    }

    /**
     * Khôi phục auto-commit về true sau khi dùng transaction.
     * Gọi trong finally block để đảm bảo connection trở về trạng thái bình thường.
     */
    public static void restoreAutoCommit(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error restoring auto-commit: " + e.getMessage());
            }
        }
    }
}