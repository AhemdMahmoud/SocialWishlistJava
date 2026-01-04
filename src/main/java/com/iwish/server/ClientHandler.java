package com.iwish.server;

import com.iwish.database.DatabaseManager;
import com.iwish.database.ItemDAO;
import com.iwish.database.FriendsDAO;
import com.iwish.models.Item;
import com.iwish.models.Friend;
import com.iwish.models.FriendRequest;
import com.iwish.models.User;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles client requests with extended protocol
 * Protocol: COMMAND##param1##param2##...
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DatabaseManager dbManager;
    private DataInputStream dis;
    private DataOutputStream dos;
    private int currentUserId = -1; // Track logged-in user

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

                    String[] parts = request.split("##");
                    String command = parts[0];

                    switch (command) {
                        case "LOGIN":
                            handleLogin(parts);
                            break;
                        case "REGISTER":
                            handleRegister(parts);
                            break;
                        case "GET_ITEMS":
                            handleGetItems();
                            break;
                        case "GET_WISHLIST":
                            handleGetWishlist(parts);
                            break;
                        case "ADD_TO_WISHLIST":
                            handleAddToWishlist(parts);
                            break;
                        case "REMOVE_FROM_WISHLIST":
                            handleRemoveFromWishlist(parts);
                            break;
                        case "GET_FRIENDS":
                            handleGetFriends(parts);
                            break;
                        case "GET_FRIEND_REQUESTS":
                            handleGetFriendRequests(parts);
                            break;
                        case "ADD_FRIEND":
                            handleAddFriend(parts);
                            break;
                        case "ACCEPT_FRIEND":
                            handleAcceptFriend(parts);
                            break;
                        case "DECLINE_FRIEND":
                            handleDeclineFriend(parts);
                            break;
                        case "REMOVE_FRIEND":
                            handleRemoveFriend(parts);
                            break;
                        case "SEARCH_USERS":
                            handleSearchUsers(parts);
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

    // ========== Authentication Commands ==========

    /**
     * LOGIN##username##password
     * Response: LOGIN_SUCCESS##userId or LOGIN_FAILED
     */
    private void handleLogin(String[] parts) throws IOException {
        if (parts.length < 3) {
            dos.writeUTF("ERROR##Invalid login format");
            return;
        }

        String username = parts[1];
        String password = parts[2];

        try {
            int userId = dbManager.loginUser(username, password);
            if (userId > 0) {
                currentUserId = userId;
                dos.writeUTF("LOGIN_SUCCESS##" + userId);
                System.out.println("User " + username + " logged in (ID: " + userId + ")");
            } else {
                dos.writeUTF("LOGIN_FAILED");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dos.writeUTF("ERROR##Database error");
        }
    }

    /**
     * REGISTER##username##password##email
     * Response: REGISTER_SUCCESS##userId or REGISTER_FAILED or ERROR
     */
    private void handleRegister(String[] parts) throws IOException {
        if (parts.length < 4) {
            dos.writeUTF("ERROR##Invalid register format");
            return;
        }

        String username = parts[1];
        String password = parts[2];
        String email = parts[3];

        try {
            int userId = dbManager.registerUser(username, password, email);
            if (userId > 0) {
                dos.writeUTF("REGISTER_SUCCESS##" + userId);
                System.out.println("New user registered: " + username + " (ID: " + userId + ")");
            } else {
                dos.writeUTF("REGISTER_FAILED##Username already exists");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dos.writeUTF("ERROR##Database error");
        }
    }

    // ========== Marketplace Commands ==========

    /**
     * GET_ITEMS
     * Response: SUCCESS##count##id:name:price:img##id:name:price:img##...
     */
    private void handleGetItems() throws IOException {
        ItemDAO itemDAO = new ItemDAO(dbManager);
        List<Item> items = itemDAO.getAllItems();

        StringBuilder response = new StringBuilder("SUCCESS##" + items.size());
        for (Item item : items) {
            response.append("##")
                    .append(item.getId()).append(":")
                    .append(item.getName()).append(":")
                    .append(item.getPrice()).append(":")
                    .append(item.getImgSrc());
        }

        dos.writeUTF(response.toString());
        dos.flush();
    }

    // ========== Wishlist Commands ==========

    /**
     * GET_WISHLIST##userId
     * Response: SUCCESS##count##id:name:price:img##id:name:price:img##...
     */
    private void handleGetWishlist(String[] parts) throws IOException {
        if (parts.length < 2) {
            dos.writeUTF("ERROR##Invalid wishlist format");
            return;
        }

        int userId = Integer.parseInt(parts[1]);
        ItemDAO itemDAO = new ItemDAO(dbManager);
        List<Item> items = itemDAO.getUserWishlist(userId);

        StringBuilder response = new StringBuilder("SUCCESS##" + items.size());
        for (Item item : items) {
            response.append("##")
                    .append(item.getId()).append(":")
                    .append(item.getName()).append(":")
                    .append(item.getPrice()).append(":")
                    .append(item.getImgSrc());
        }

        dos.writeUTF(response.toString());
        dos.flush();
    }

    /**
     * ADD_TO_WISHLIST##userId##itemId
     * Response: SUCCESS or ALREADY_EXISTS or ERROR
     */
    private void handleAddToWishlist(String[] parts) throws IOException {
        if (parts.length < 3) {
            dos.writeUTF("ERROR##Invalid format");
            return;
        }

        int userId = Integer.parseInt(parts[1]);
        int itemId = Integer.parseInt(parts[2]);

        ItemDAO itemDAO = new ItemDAO(dbManager);
        boolean added = itemDAO.addToUserWishlist(userId, itemId);

        if (added) {
            dos.writeUTF("SUCCESS");
            System.out.println("User " + userId + " added item " + itemId + " to wishlist");
        } else {
            dos.writeUTF("ALREADY_EXISTS");
        }
    }

    /**
     * REMOVE_FROM_WISHLIST##userId##itemId
     * Response: SUCCESS or ERROR
     */
    private void handleRemoveFromWishlist(String[] parts) throws IOException {
        if (parts.length < 3) {
            dos.writeUTF("ERROR##Invalid format");
            return;
        }

        int userId = Integer.parseInt(parts[1]);
        int itemId = Integer.parseInt(parts[2]);

        ItemDAO itemDAO = new ItemDAO(dbManager);
        boolean removed = itemDAO.removeFromUserWishlist(userId, itemId);

        if (removed) {
            dos.writeUTF("SUCCESS");
            System.out.println("User " + userId + " removed item " + itemId + " from wishlist");
        } else {
            dos.writeUTF("ERROR##Item not found in wishlist");
        }
    }

    // ========== Friends Commands ==========

    /**
     * GET_FRIENDS##userId
     * Response: SUCCESS##count##userId:username##userId:username##...
     */
    private void handleGetFriends(String[] parts) throws IOException {
        if (parts.length < 2) {
            dos.writeUTF("ERROR##Invalid format");
            return;
        }

        int userId = Integer.parseInt(parts[1]);
        FriendsDAO friendsDAO = new FriendsDAO(dbManager);
        List<Friend> friends = friendsDAO.getFriends(userId);

        StringBuilder response = new StringBuilder("SUCCESS##" + friends.size());
        for (Friend friend : friends) {
            response.append("##")
                    .append(friend.getUserId()).append(":")
                    .append(friend.getUsername());
        }

        dos.writeUTF(response.toString());
        dos.flush();
    }

    /**
     * GET_FRIEND_REQUESTS##userId
     * Response: SUCCESS##count##friendshipId:userId:username:date##...
     */
    private void handleGetFriendRequests(String[] parts) throws IOException {
        if (parts.length < 2) {
            dos.writeUTF("ERROR##Invalid format");
            return;
        }

        int userId = Integer.parseInt(parts[1]);
        FriendsDAO friendsDAO = new FriendsDAO(dbManager);
        List<FriendRequest> requests = friendsDAO.getFriendRequests(userId);

        StringBuilder response = new StringBuilder("SUCCESS##" + requests.size());
        for (FriendRequest req : requests) {
            response.append("##")
                    .append(req.getFriendshipId()).append(":")
                    .append(req.getUserId()).append(":")
                    .append(req.getUsername()).append(":")
                    .append(req.getRequestDate().getTime());
        }

        dos.writeUTF(response.toString());
        dos.flush();
    }

    /**
     * ADD_FRIEND##userId##friendUsername
     * Response: SUCCESS or ERROR##reason
     */
    private void handleAddFriend(String[] parts) throws IOException {
        if (parts.length < 3) {
            dos.writeUTF("ERROR##Invalid format");
            return;
        }

        int userId = Integer.parseInt(parts[1]);
        String friendUsername = parts[2];

        FriendsDAO friendsDAO = new FriendsDAO(dbManager);
        boolean sent = friendsDAO.sendFriendRequest(userId, friendUsername);

        if (sent) {
            dos.writeUTF("SUCCESS");
            System.out.println("User " + userId + " sent friend request to " + friendUsername);
        } else {
            dos.writeUTF("ERROR##User not found or request already exists");
        }
    }

    /**
     * ACCEPT_FRIEND##friendshipId
     * Response: SUCCESS or ERROR
     */
    private void handleAcceptFriend(String[] parts) throws IOException {
        if (parts.length < 2) {
            dos.writeUTF("ERROR##Invalid format");
            return;
        }

        int friendshipId = Integer.parseInt(parts[1]);
        FriendsDAO friendsDAO = new FriendsDAO(dbManager);
        boolean accepted = friendsDAO.acceptFriendRequest(friendshipId);

        if (accepted) {
            dos.writeUTF("SUCCESS");
            System.out.println("Friend request " + friendshipId + " accepted");
        } else {
            dos.writeUTF("ERROR##Failed to accept request");
        }
    }

    /**
     * DECLINE_FRIEND##friendshipId
     * Response: SUCCESS or ERROR
     */
    private void handleDeclineFriend(String[] parts) throws IOException {
        if (parts.length < 2) {
            dos.writeUTF("ERROR##Invalid format");
            return;
        }

        int friendshipId = Integer.parseInt(parts[1]);
        FriendsDAO friendsDAO = new FriendsDAO(dbManager);
        boolean declined = friendsDAO.declineFriendRequest(friendshipId);

        if (declined) {
            dos.writeUTF("SUCCESS");
            System.out.println("Friend request " + friendshipId + " declined");
        } else {
            dos.writeUTF("ERROR##Failed to decline request");
        }
    }

    /**
     * REMOVE_FRIEND##userId##friendId
     * Response: SUCCESS or ERROR
     */
    private void handleRemoveFriend(String[] parts) throws IOException {
        if (parts.length < 3) {
            dos.writeUTF("ERROR##Invalid format");
            return;
        }

        int userId = Integer.parseInt(parts[1]);
        int friendId = Integer.parseInt(parts[2]);

        FriendsDAO friendsDAO = new FriendsDAO(dbManager);
        boolean removed = friendsDAO.removeFriend(userId, friendId);

        if (removed) {
            dos.writeUTF("SUCCESS");
            System.out.println("User " + userId + " removed friend " + friendId);
        } else {
            dos.writeUTF("ERROR##Failed to remove friend");
        }
    }

    /**
     * SEARCH_USERS##query
     * Response: SUCCESS##count##userId:username##userId:username##...
     */
    private void handleSearchUsers(String[] parts) throws IOException {
        if (parts.length < 2) {
            dos.writeUTF("ERROR##Invalid format");
            return;
        }

        String query = parts[1];
        FriendsDAO friendsDAO = new FriendsDAO(dbManager);
        List<User> users = friendsDAO.searchUsers(query);

        StringBuilder response = new StringBuilder("SUCCESS##" + users.size());
        for (User user : users) {
            response.append("##")
                    .append(user.getUserId()).append(":")
                    .append(user.getUsername());
        }

        dos.writeUTF(response.toString());
        dos.flush();
    }
}
