package com.iwish.server;

import com.iwish.database.DatabaseManager;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DatabaseManager dbManager;
    private DataInputStream dis;
    private DataOutputStream dos;

    public ClientHandler(Socket socket, DatabaseManager dbManager) {
        this.socket = socket;
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            while (true) {
                // Keep the connection open - loop waiting for commands
                try {
                    String request = dis.readUTF();
                    System.out.println("Received: " + request);

                    // TODO: Implement command handling (Login, Register, etc.)
                    // Example:
                    // String[] parts = request.split(":");
                    // String command = parts[0];
                    // switch(command) { ... }

                } catch (IOException e) {
                    System.out.println("Client disconnected.");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null && !socket.isClosed())
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
