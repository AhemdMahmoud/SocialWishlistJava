package com.iwish.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbTester {
    public static void main(String[] args) {
        System.out.println("=== Database Connection Tester ===");
        String url = "jdbc:derby://localhost:1527/iwishdb";
        String user = "root";
        String pass = "root";

        System.out.println("Attempting to connect to: " + url);
        try {
            // Test Driver Load
            try {
                Class.forName("org.apache.derby.jdbc.ClientDriver");
                System.out.println("✅ Driver Loaded: org.apache.derby.jdbc.ClientDriver");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver NOT Found! Check pom.xml dependencies.");
                e.printStackTrace();
                return;
            }

            // Test Connection
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ Connection Successful!");
            System.out.println("Connected to: " + conn.getMetaData().getURL());
            conn.close();
        } catch (SQLException e) {
            System.err.println("❌ Connection Failed!");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
