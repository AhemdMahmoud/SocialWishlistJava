package com.iwish.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.InetAddress;
import java.io.PrintWriter;
import org.apache.derby.drda.NetworkServerControl;

/**
 * Handles initialization of the Database.
 * Connects to Derby Network Server and initializes tables from
 * database/schema.sql
 */
public class DatabaseManager {
    // HikariCP DataSource
    private static com.zaxxer.hikari.HikariDataSource dataSource;
    private static NetworkServerControl serverControl;

    // Database configuration - Network Derby Server
    private static final String DB_URL = "jdbc:derby://localhost:1527/iwishdb;create=true";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public boolean connect() {
        startNetworkServer();
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
                    System.out.println("Tables not found. Initializing database...");
                    initializeDatabase(conn);
                } else {
                    checkAndSeedData(conn);
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
        if (serverControl != null) {
            try {
                serverControl.shutdown();
                System.out.println("Derby Network Server stopped.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startNetworkServer() {
        try {
            InetAddress host = InetAddress.getByName("localhost");
            serverControl = new NetworkServerControl(host, 1527);
            
            // Checks if already running
            try {
                serverControl.ping();
                System.out.println("Derby Network Server is already running.");
                return;
            } catch (Exception e) {
                // Not running, so start it
            }

            serverControl.start(new PrintWriter(System.out, true));
            System.out.println("Derby Network Server started on localhost:1527");

            // Wait for server to start
            int retries = 0;
            while (retries < 10) {
                try {
                    Thread.sleep(500);
                    serverControl.ping();
                    break;
                } catch (Exception e) {
                    retries++;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to start Derby Network Server: " + e.getMessage());
            e.printStackTrace();
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
    private void initializeDatabase(Connection conn) {
        // ... (existing implementation)
        runScript(conn, new File("database/schema.sql"));
        System.out.println("Database tables created. Checking for seed data...");
        checkAndSeedData(conn);
    }

    private void checkAndSeedData(Connection conn) {
        try (Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM WISHLIST")) {
            if (rs.next()) {
                int count = rs.getInt(1);
                // If we have stale data (e.g., just the initial 6 items), we wipe and re-seed
                if (count < 10) { 
                    System.out.println("Detected partial/stale data (Count: " + count + "). Re-seeding database...");
                    try {
                        stmt.executeUpdate("DELETE FROM WISHLIST"); // Clear old data
                        // Reset identity if supported by Derby, or just accept gaps. 
                        // Derby doesn't easily reset identity without procedure call, simple delete is fine.
                    } catch (SQLException e) {
                        System.err.println("Warning: Failed to clear table, but attempting seed anyway: " + e.getMessage());
                    }
                    runScript(conn, new File("database/seed_data.sql"));
                } else {
                    System.out.println("Wishlist data verified (Count: " + count + "). Skipping seed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void runScript(Connection conn, File scriptFile) {
        if (!scriptFile.exists()) {
             System.out.println("Script file not found at " + scriptFile.getAbsolutePath());
             return;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(scriptFile));
             Statement stmt = conn.createStatement()) {
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                // Skip markdown code blocks if present
                if (trimmed.startsWith("```")) continue;
                if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                
                sb.append(line).append(" ");
                if (trimmed.endsWith(";")) {
                    String sql = sb.toString().replace(";", "").trim();
                    try {
                        stmt.execute(sql);
                        System.out.println("Executed: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                    } catch (SQLException e) {
                        System.err.println("Error executing: " + sql);
                    }
                    sb = new StringBuilder();
                }
            }
            System.out.println("Script execution complete: " + scriptFile.getName());
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
