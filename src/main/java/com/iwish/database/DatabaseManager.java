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
    private static final String DB_URL = "jdbc:derby://localhost:1527/iwish_db;create=true";
    private static final String USER = "root"; // Using generic root/root as per typical setup
    private static final String PASSWORD = "root";

    private Connection connection;

    public boolean connect() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);

            if (!tablesExist()) {
                System.out.println("Tables not found. Initializing database from schema.sql...");
                createTables();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private boolean tablesExist() {
        try {
            // Check if USERS table exists
            return connection.getMetaData().getTables(null, null, "USERS", null).next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void createTables() {
        File schemaFile = new File("database/schema.sql");
        if (!schemaFile.exists()) {
            System.err.println("CRITICAL ERROR: database/schema.sql not found!");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(schemaFile));
                Statement stmt = connection.createStatement()) {

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments
                if (line.trim().startsWith("--"))
                    continue;
                if (line.trim().isEmpty())
                    continue;

                sql.append(line).append(" ");

                if (line.trim().endsWith(";")) {
                    String command = sql.toString().replace(";", "").trim();
                    try {
                        stmt.execute(command);
                        System.out.println("Executed: " + command.substring(0, Math.min(command.length(), 30)) + "...");
                    } catch (SQLException e) {
                        System.err.println("Error executing: " + command);
                        e.printStackTrace();
                    }
                    sql = new StringBuilder();
                }
            }
            System.out.println("Database initialization complete.");

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
