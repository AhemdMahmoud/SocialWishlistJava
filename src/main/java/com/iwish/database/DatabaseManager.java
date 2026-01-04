package com.iwish.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles initialization of the Database.
 * Connects to Derby Network Server and initializes tables from
 * database/schema.sql
 */
public class DatabaseManager {
    // HikariCP DataSource
    private static com.zaxxer.hikari.HikariDataSource dataSource;

    // Database configuration - Network Derby Server
    private static final String DB_URL = "jdbc:derby://localhost:1527/iwishdb";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public boolean connect() {
        try {
            if (dataSource == null || dataSource.isClosed()) {
                com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
                config.setJdbcUrl(DB_URL);
                config.setUsername(USER);
                config.setPassword(PASSWORD);
                config.setDriverClassName("org.apache.derby.jdbc.ClientDriver");

                // HikariCP settings (basic tuning)
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setIdleTimeout(30000);
                config.setConnectionTimeout(3000);

                dataSource = new com.zaxxer.hikari.HikariDataSource(config);
            }

            // Test connection
            try (Connection conn = dataSource.getConnection()) {
                if (!tablesExist(conn)) {
                    System.out.println("Tables not found. Please run migration script first!");
                    System.out.println("See: database/MIGRATION_TO_NETWORK_DERBY.sql");
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not initialized. Call connect() first.");
        }
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database pool closed.");
        }
    }

    private boolean tablesExist(Connection conn) {
        try {
            return conn.getMetaData().getTables(null, null, "USERS", null).next();
        } catch (SQLException e) {
            return false;
        }
    }

    // ========== Authentication Methods ==========

    /**
     * Register a new user
     * 
     * @return user_id if successful, -1 if username already exists
     */
    public int registerUser(String username, String password, String email) throws SQLException {
        if (dataSource == null)
            connect();

        String sql = "INSERT INTO Users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pst = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, username);
            pst.setString(2, password); // TODO: Hash password in production
            pst.setString(3, email);
            pst.executeUpdate();

            java.sql.ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Unique constraint violation
                return -1; // Username already exists
            }
            throw e;
        }
        return -1;
    }

    /**
     * Authenticate user login
     * 
     * @return user_id if successful, -1 if failed
     */
    public int loginUser(String username, String password) throws SQLException {
        if (dataSource == null)
            connect();

        String sql = "SELECT user_id FROM Users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username);
            pst.setString(2, password); // TODO: Hash comparison in production

            try (java.sql.ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        return -1; // Login failed
    }
}
