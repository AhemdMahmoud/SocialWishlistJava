package com.iwish.database;

import com.iwish.models.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private final DatabaseManager dbManager;

    public NotificationDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void createNotification(int userId, String type, String message, Integer relatedId) {
        String sql = "INSERT INTO Notifications (user_id, type, message, related_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            pstmt.setString(3, message);

            if (relatedId != null) {
                pstmt.setInt(4, relatedId);
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getNotificationsForUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT notification_id, user_id, type, message, related_id, is_read, notification_date " +
                     "FROM Notifications WHERE user_id = ? ORDER BY notification_date DESC";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Notification n = new Notification();
                n.setNotificationId(rs.getInt("notification_id"));
                n.setUserId(rs.getInt("user_id"));
                n.setType(rs.getString("type"));
                n.setMessage(rs.getString("message"));

                int related = rs.getInt("related_id");
                if (rs.wasNull()) {
                    n.setRelatedId(null);
                } else {
                    n.setRelatedId(related);
                }

                int isRead = rs.getInt("is_read");
                n.setRead(isRead == 1);

                n.setNotificationDate(rs.getTimestamp("notification_date"));

                notifications.add(n);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notifications;
    }

    public boolean markNotificationRead(int notificationId) {
        String sql = "UPDATE Notifications SET is_read = 1 WHERE notification_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notificationId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}