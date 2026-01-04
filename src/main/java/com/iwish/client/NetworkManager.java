package com.iwish.client;

import com.iwish.models.Item;
import com.iwish.models.Friend;
import com.iwish.models.FriendRequest;
import com.iwish.models.User;
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

    // ========== Friends Methods ==========

    /**
     * Get list of accepted friends
     * 
     * @return List of Friend objects
     */
    public List<Friend> getFriends(int userId) {
        List<Friend> friends = new ArrayList<>();
        try {
            dos.writeUTF("GET_FRIENDS##" + userId);
            String response = dis.readUTF();

            String[] parts = response.split("##");
            if ("SUCCESS".equals(parts[0])) {
                for (int i = 2; i < parts.length; i++) {
                    String friendStr = parts[i];
                    String[] friendParts = friendStr.split(":");

                    if (friendParts.length >= 2) {
                        try {
                            int friendUserId = Integer.parseInt(friendParts[0]);
                            String username = friendParts[1];
                            friends.add(new Friend(friendUserId, username));
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing friend: " + friendStr);
                        }
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
     * 
     * @return List of FriendRequest objects
     */
    public List<FriendRequest> getFriendRequests(int userId) {
        List<FriendRequest> requests = new ArrayList<>();
        try {
            dos.writeUTF("GET_FRIEND_REQUESTS##" + userId);
            String response = dis.readUTF();

            String[] parts = response.split("##");
            if ("SUCCESS".equals(parts[0])) {
                for (int i = 2; i < parts.length; i++) {
                    String reqStr = parts[i];
                    String[] reqParts = reqStr.split(":");

                    if (reqParts.length >= 3) {
                        try {
                            int friendshipId = Integer.parseInt(reqParts[0]);
                            int fromUserId = Integer.parseInt(reqParts[1]);
                            String username = reqParts[2];
                            // Note: Request date handling might be needed if sent by server, skipping for
                            // now as per simplicity or add if needed
                            // Constructing FriendRequest with 0 or minimal args if server doesn't send date
                            // Assuming model has constructor: FriendRequest(int friendshipId, int
                            // fromUserId, String username, Timestamp requestDate)
                            // We might need to adjust this if we don't have date from server text protocol
                            // easily, or parse it.
                            // For now, let's pass null for date or update server to send it properly if
                            // needed.
                            // The server protocol says: friendshipId:userId:username:date

                            java.sql.Timestamp date = null;
                            if (reqParts.length >= 4) {
                                // Simple parsing or just null for now to avoid format errors unless we know
                                // format
                                // date = java.sql.Timestamp.valueOf(reqParts[3]);
                            }

                            requests.add(new FriendRequest(friendshipId, fromUserId, username, date));
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing request: " + reqStr);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Send friend request to user
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
     * Remove existing friend
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
     * Search for users by username
     */
    public List<User> searchUsers(String query) {
        List<User> users = new ArrayList<>();
        try {
            dos.writeUTF("SEARCH_USERS##" + query);
            String response = dis.readUTF();

            String[] parts = response.split("##");
            if ("SUCCESS".equals(parts[0])) {
                for (int i = 2; i < parts.length; i++) {
                    String userStr = parts[i];
                    String[] userParts = userStr.split(":");

                    if (userParts.length >= 2) {
                        try {
                            int userId = Integer.parseInt(userParts[0]);
                            String username = userParts[1];
                            users.add(new User(userId, username));
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing user: " + userStr);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
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

    public void close() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
