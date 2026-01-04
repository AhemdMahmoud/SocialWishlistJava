package com.iwish.database;

import com.iwish.models.Friend;
import com.iwish.models.FriendRequest;
import com.iwish.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Friends management
 */
public class FriendsDAO {
    private final DatabaseManager dbManager;

    public FriendsDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Get all accepted friends for a user
     */
    public List<Friend> getFriends(int userId) {
        List<Friend> friends = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.email, f.friendship_id " +
                "FROM FRIENDS f " +
                "JOIN USERS u ON (f.friend_id = u.user_id OR f.user_id = u.user_id) " +
                "WHERE (f.user_id = ? OR f.friend_id = ?) " +
                "AND f.status = 'accepted' " +
                "AND u.user_id != ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Friend friend = new Friend(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getInt("friendship_id"));
                friends.add(friend);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    /**
     * Get pending friend requests for a user (requests sent TO this user)
     */
    public List<FriendRequest> getFriendRequests(int userId) {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT f.friendship_id, u.user_id, u.username, u.email, f.request_date " +
                "FROM FRIENDS f " +
                "JOIN USERS u ON f.user_id = u.user_id " +
                "WHERE f.friend_id = ? AND f.status = 'pending'";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                FriendRequest request = new FriendRequest(
                        rs.getInt("friendship_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getTimestamp("request_date"));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Send friend request
     */
    public boolean sendFriendRequest(int userId, int friendId) {
        // Check if already friends or request exists
        String checkSql = "SELECT friendship_id FROM FRIENDS " +
                "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, friendId);
            checkStmt.setInt(3, friendId);
            checkStmt.setInt(4, userId);

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return false; // Already exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Send request
        String sql = "INSERT INTO FRIENDS (user_id, friend_id, status) VALUES (?, ?, 'pending')";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, friendId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Accept friend request
     */
    public boolean acceptFriendRequest(int friendshipId) {
        String sql = "UPDATE FRIENDS SET status = 'accepted' WHERE friendship_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, friendshipId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decline friend request
     */
    public boolean declineFriendRequest(int friendshipId) {
        String sql = "DELETE FROM FRIENDS WHERE friendship_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, friendshipId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove friend (delete friendship)
     */
    public boolean removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM FRIENDS " +
                "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, friendId);
            pstmt.setInt(3, friendId);
            pstmt.setInt(4, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Search users by username
     */
    public List<User> searchUsers(String query) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, email FROM USERS " +
                "WHERE LOWER(username) LIKE LOWER(?) " +
                "ORDER BY username LIMIT 20";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + query + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}
