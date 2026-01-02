package com.iwish.database;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:derby://localhost:1527/iwishdb";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private Connection connection;

    public DatabaseManager() throws SQLException {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Derby JDBC Driver not found", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // --- Authentication Operations ---

    public int registerUser(String username, String password, String email) throws SQLException {
        // First check if table exists, if not, create it (Safety check)
        checkTables();

        String sql = "INSERT INTO Users (username, password, email) VALUES (?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, email);
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Unique constraint violation
                return -1;
            }
            throw e;
        }
        return -1;
    }

    public ResultSet loginUser(String username, String password) throws SQLException {
        checkTables();
        String sql = "SELECT user_id, username, email FROM Users WHERE username = ? AND password = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, username);
        pst.setString(2, password);
        return pst.executeQuery(); // Caller must close ResultSet/PreparedStatement or handle appropriately
    }

    private void checkTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "USERS", null);
            if (!tables.next()) {
                // Table does not exist, create it
                // Note: Enforcing strict schema from prompt
                stmt.execute("CREATE TABLE Users (" +
                        "user_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                        "username VARCHAR(50) UNIQUE NOT NULL, " +
                        "password VARCHAR(50) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL)");
            }
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
