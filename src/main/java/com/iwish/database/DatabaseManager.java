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

    // Database configuration
    // Using Embedded Driver so no separate server process is needed
    private static final String DB_URL = "jdbc:derby:iwish_db;create=true";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public boolean connect() {
        try {
            if (dataSource == null || dataSource.isClosed()) {
                com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
                config.setJdbcUrl(DB_URL);
                config.setUsername(USER);
                config.setPassword(PASSWORD);
                config.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
                
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
                    System.out.println("Tables not found. Initializing database from schema.sql...");
                    createTables(conn);
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

    private void createTables(Connection conn) {
        File schemaFile = new File("database/schema.sql");
        if (!schemaFile.exists()) {
            System.err.println("CRITICAL ERROR: database/schema.sql not found!");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(schemaFile));
                Statement stmt = conn.createStatement()) {

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("--") || line.trim().isEmpty()) continue;
                
                sql.append(line).append(" ");
                if (line.trim().endsWith(";")) {
                    String command = sql.toString().replace(";", "").trim();
                    try {
                        stmt.execute(command);
                        System.out.println("Executed: " + command.substring(0, Math.min(command.length(), 30)) + "...");
                    } catch (SQLException e) {
                        System.err.println("Error executing: " + command);
                         // Don't fail entire script on one error (e.g. drop table if exists)
                    }
                    sql = new StringBuilder();
                }
            }
            System.out.println("Database initialization complete.");
            
            // Seed initial data
            seedData(conn);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void seedData(Connection conn) {
        File seedFile = new File("src/main/java/com/iwish/database/seed_data.sql");
        if (!seedFile.exists()) {
            System.out.println("No seed data found.");
            return;
        }
        System.out.println("Seeding database...");
        // Re-use logic or just run the script. For simplicity, similar logic:
        try (BufferedReader reader = new BufferedReader(new FileReader(seedFile));
             Statement stmt = conn.createStatement()) {
             
             // Check if items already exist to avoid duplicates
             try (java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM WISHLIST")) {
                 if (rs.next() && rs.getInt(1) > 0) {
                     System.out.println("Database already contains items. Skipping seed.");
                     return;
                 }
             }

             StringBuilder sql = new StringBuilder();
             String line;
             while ((line = reader.readLine()) != null) {
                 if (line.trim().startsWith("--") || line.trim().isEmpty()) continue;
                 sql.append(line).append(" ");
                 if (line.trim().endsWith(";")) {
                     String command = sql.toString().replace(";", "").trim();
                     try {
                         stmt.execute(command);
                     } catch (SQLException e) {
                         System.err.println("Error seeding: " + command);
                     }
                     sql = new StringBuilder();
                 }
             }
             System.out.println("Seeding complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
