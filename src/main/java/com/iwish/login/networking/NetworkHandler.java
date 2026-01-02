package com.iwish.login.networking;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class NetworkHandler {
    private static NetworkHandler instance;
    private Socket socket;
    private DataInputStream dis;
    private PrintStream ps;

    private NetworkHandler() {
    }

    public static synchronized NetworkHandler getInstance() {
        if (instance == null) {
            instance = new NetworkHandler();
        }
        return instance;
    }

    public boolean connect() {
        try {
            if (socket != null && !socket.isClosed()) {
                return true;
            }
            socket = new Socket("127.0.0.1", 5005);
            dis = new DataInputStream(socket.getInputStream());
            ps = new PrintStream(socket.getOutputStream());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String sendRequest(String request) throws IOException {
        if (ps == null || socket == null || socket.isClosed()) {
            throw new IOException("Not connected to server");
        }
        ps.println(request);
        return dis.readLine();
    }

    public void close() {
        try {
            if (dis != null)
                dis.close();
            if (ps != null)
                ps.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
