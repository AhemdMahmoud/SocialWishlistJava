package com.iwish.client;

import com.iwish.models.Item;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * NetworkManager - Client-side network communication
 * Singleton pattern for managing server connection
 */
public class NetworkManager {
    private static NetworkManager instance;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 5000;

    // Current logged-in user
    private int currentUserId = -1;
    private String currentUsername = "";

    private NetworkManager() {
    }

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public boolean connect() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== Authentication Methods ==========

    /**
     * Login user
     * 
     * @return user_id if successful, -1 if failed
     */
    public int login(String username, String password) {
        try {
            dos.writeUTF("LOGIN##" + username + "##" + password);
            String response = dis.readUTF();

            String[] parts = response.split("##");
            if ("LOGIN_SUCCESS".equals(parts[0])) {
                currentUserId = Integer.parseInt(parts[1]);
                currentUsername = username;
                return currentUserId;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Register new user
     * 
     * @return user_id if successful, -1 if failed
     */
    public int register(String username, String password, String email) {
        try {
            dos.writeUTF("REGISTER##" + username + "##" + password + "##" + email);
            String response = dis.readUTF();

            String[] parts = response.split("##");
            if ("REGISTER_SUCCESS".equals(parts[0])) {
                currentUserId = Integer.parseInt(parts[1]);
                currentUsername = username;
                return currentUserId;
            } else if ("REGISTER_FAILED".equals(parts[0])) {
                System.err.println("Registration failed: " + (parts.length > 1 ? parts[1] : "Unknown error"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ========== Marketplace Methods ==========

    /**
     * Get all items from marketplace catalog
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        try {
            dos.writeUTF("GET_ITEMS");
            String response = dis.readUTF();

            String[] parts = response.split("##");
            if ("SUCCESS".equals(parts[0])) {
                for (int i = 2; i < parts.length; i++) {
                    String itemStr = parts[i];
                    String[] itemParts = itemStr.split(":");

                    if (itemParts.length >= 3) {
                        try {
                            int id = Integer.parseInt(itemParts[0]);
                            String name = itemParts[1];
                            double price = Double.parseDouble(itemParts[2]);
                            String imgSrc = itemParts.length >= 4 ? itemParts[3] : "";

                            items.add(new Item(id, name, price, imgSrc));
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing item: " + itemStr);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    // ========== Wishlist Methods ==========

    /**
     * Get user's personal wishlist
     */
    public List<Item> getUserWishlist(int userId) {
        List<Item> items = new ArrayList<>();
        try {
            dos.writeUTF("GET_WISHLIST##" + userId);
            String response = dis.readUTF();

            String[] parts = response.split("##");
            if ("SUCCESS".equals(parts[0])) {
                for (int i = 2; i < parts.length; i++) {
                    String itemStr = parts[i];
                    String[] itemParts = itemStr.split(":");

                    if (itemParts.length >= 3) {
                        try {
                            int id = Integer.parseInt(itemParts[0]);
                            String name = itemParts[1];
                            double price = Double.parseDouble(itemParts[2]);
                            String imgSrc = itemParts.length >= 4 ? itemParts[3] : "";

                            items.add(new Item(id, name, price, imgSrc));
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing item: " + itemStr);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Add item from marketplace to user's wishlist
     */
    public boolean addToWishlist(int userId, int itemId) {
        try {
            dos.writeUTF("ADD_TO_WISHLIST##" + userId + "##" + itemId);
            String response = dis.readUTF();
            return "SUCCESS".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove item from user's wishlist
     */
    public boolean removeFromWishlist(int userId, int itemId) {
        try {
            dos.writeUTF("REMOVE_FROM_WISHLIST##" + userId + "##" + itemId);
            String response = dis.readUTF();
            return "SUCCESS".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== Getters ==========

    public int getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    // ========== Friends Methods ==========

    /**
     * Get user's friends list
     */
    public List<com.iwish.models.Friend> getFriends(int userId) {
        List<com.iwish.models.Friend> friends = new ArrayList<>();
        try {
            dos.writeUTF("GET_FRIENDS##" + userId);
            String response = dis.readUTF();

            String[] parts = response.split("##");
            if ("SUCCESS".equals(parts[0])) {
                for (int i = 2; i < parts.length; i++) {
                    String[] friendData = parts[i].split(":");
                    if (friendData.length >= 2) {
                        int friendId = Integer.parseInt(friendData[0]);
                        String username = friendData[1];
                        friends.add(new com.iwish.models.Friend(friendId, username, "", 0));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return friends;
    }

    /**
     * Get pending friend requests
     */
    public List<com.iwish.models.FriendRequest> getFriendRequests(int userId) {
        List<com.iwish.models.FriendRequest> requests = new ArrayList<>();
        try {
            dos.writeUTF("GET_FRIEND_REQUESTS##" + userId);
            String response = dis.readUTF();

            String[] parts = response.split("##");
            if ("SUCCESS".equals(parts[0])) {
                for (int i = 2; i < parts.length; i++) {
                    String[] requestData = parts[i].split(":");
                    if (requestData.length >= 2) {
                        int friendshipId = Integer.parseInt(requestData[0]);
                        String username = requestData[1];
                        requests.add(new com.iwish.models.FriendRequest(
                                friendshipId, 0, username, "", null));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Send friend request by username
     */
    public boolean addFriend(int userId, String friendUsername) {
        try {
            dos.writeUTF("ADD_FRIEND##" + userId + "##" + friendUsername);
            String response = dis.readUTF();
            return "SUCCESS".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Accept friend request
     */
    public boolean acceptFriendRequest(int friendshipId) {
        try {
            dos.writeUTF("ACCEPT_FRIEND##" + friendshipId);
            String response = dis.readUTF();
            return "SUCCESS".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decline friend request
     */
    public boolean declineFriendRequest(int friendshipId) {
        try {
            dos.writeUTF("DECLINE_FRIEND##" + friendshipId);
            String response = dis.readUTF();
            return "SUCCESS".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove friend
     */
    public boolean removeFriend(int userId, int friendId) {
        try {
            dos.writeUTF("REMOVE_FRIEND##" + userId + "##" + friendId);
            String response = dis.readUTF();
            return "SUCCESS".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Search users by username
     */
    public List<com.iwish.models.User> searchUsers(String query) {
        List<com.iwish.models.User> users = new ArrayList<>();
        try {
            dos.writeUTF("SEARCH_USERS##" + query);
            String response = dis.readUTF();

            String[] parts = response.split("##");
            if ("SUCCESS".equals(parts[0])) {
                for (int i = 2; i < parts.length; i++) {
                    String[] userData = parts[i].split(":");
                    if (userData.length >= 2) {
                        int userId = Integer.parseInt(userData[0]);
                        String username = userData[1];
                        users.add(new com.iwish.models.User(userId, username, ""));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void close() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
