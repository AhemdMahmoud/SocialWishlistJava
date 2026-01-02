package com.iwish.server;

import com.iwish.database.DatabaseManager;
import com.iwish.database.ItemDAO;
import com.iwish.models.Item;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream; // For sending list objects if needed, but simple protocol preferred
import java.net.Socket;
import java.util.List;

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
                try {
                    String request = dis.readUTF();
                    System.out.println("Received: " + request);

                    String[] parts = request.split("##"); // Using ## as delimiter
                    String command = parts[0];

                    switch (command) {
                        case "GET_ITEMS":
                            handleGetItems();
                            break;
                        default:
                            dos.writeUTF("ERROR##Unknown Command");
                            break;
                    }
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


    private void handleGetItems() throws IOException {
        ItemDAO itemDAO = new ItemDAO(dbManager);
        List<Item> items = itemDAO.getAllItems();
        
        // Simple serialization: JSON-like or just delimited
        // Protocol: SUCCESS##Count##ID:Name:Price:ImgSrc##ID:Name:Price:ImgSrc...
        StringBuilder response = new StringBuilder("SUCCESS##" + items.size());
                for (Item item : items) {
                // Protocol: ID:Name:Price:ImgSrc##...
                // Ensure no colons in content or handle escaping if needed.
                response.append("##")
                  .append(item.getId()).append(":")
                  .append(item.getName()).append(":")
                  .append(item.getPrice()).append(":")
                  .append(item.getImgSrc());
            }
        
        dos.writeUTF(response.toString());
        dos.flush();
    }
}
