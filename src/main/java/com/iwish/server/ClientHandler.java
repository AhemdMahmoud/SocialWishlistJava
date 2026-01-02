package com.iwish.server;

import com.iwish.database.DatabaseManager;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientHandler extends Thread {
    private Socket socket;
    private DataInputStream dataInputStream;
    private PrintStream printStream;
    private DatabaseManager dbManager;
    private int userId = -1;
    private String username = "";
    private boolean online = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            printStream = new PrintStream(socket.getOutputStream());
            try {
                dbManager = new DatabaseManager();
            } catch (SQLException e) {
                System.err.println("Database Connection Failed for Client: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (online) {
                String request = dataInputStream.readLine();
                if (request == null)
                    break;

                System.out.println("Received: " + request);
                processRequest(request);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + (username.isEmpty() ? socket.getInetAddress() : username));
        } finally {
            cleanup();
        }
    }

    private void processRequest(String request) {
        String[] parts = request.split(":");
        String command = parts[0];

        try {
            switch (command) {
                case "REGISTER":
                    handleRegister(parts);
                    break;
                case "LOGIN":
                    handleLogin(parts);
                    break;
                default:
                    sendResponse("ERROR:Unknown command");
            }
        } catch (Throwable e) {
            System.err.println("Error processing request: " + command);
            e.printStackTrace();
            sendResponse("ERROR:Server error - " + e.getMessage());
        }
    }

    // REGISTER:username:password:email
    private void handleRegister(String[] parts) throws SQLException {
        if (dbManager == null) {
            sendResponse("ERROR:Server Database Unavailable");
            return;
        }
        if (parts.length < 4) {
            sendResponse("ERROR:Invalid registration format");
            return;
        }
        String username = parts[1];
        String password = parts[2];
        String email = parts[3];

        int newId = dbManager.registerUser(username, password, email);
        if (newId > 0) {
            sendResponse("SUCCESS:Registration completed");
        } else {
            sendResponse("ERROR:Username already exists");
        }
    }

    // LOGIN:username:password
    private void handleLogin(String[] parts) throws SQLException {
        if (dbManager == null) {
            sendResponse("ERROR:Server Database Unavailable");
            return;
        }
        if (parts.length < 3) {
            sendResponse("ERROR:Invalid login format");
            return;
        }
        String username = parts[1];
        String password = parts[2];

        try (ResultSet rs = dbManager.loginUser(username, password)) {
            if (rs != null && rs.next()) {
                this.userId = rs.getInt("user_id");
                this.username = username;
                sendResponse("SUCCESS:" + userId + ":" + username);
            } else {
                sendResponse("ERROR:Invalid credentials");
            }
        }
    }

    private void sendResponse(String response) {
        if (printStream != null) {
            printStream.println(response);
        }
    }

    private void cleanup() {
        online = false;
        try {
            if (dbManager != null)
                dbManager.close();
            if (dataInputStream != null)
                dataInputStream.close();
            if (printStream != null)
                printStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ServerMain.removeClient(this);
    }
}
