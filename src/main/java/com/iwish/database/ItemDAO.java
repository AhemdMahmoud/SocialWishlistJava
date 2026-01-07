package com.iwish.database;

import com.iwish.models.Item;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for WishList items (Shared Catalog Model)
 * - WishList = Global marketplace catalog
 * - UserWishes = User's personal selections
 */
public class ItemDAO {
    private final DatabaseManager dbManager;

    public ItemDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Get ALL items from marketplace catalog (for home/marketplace view)
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM WISHLIST";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                items.add(new Item(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getDouble("item_price"),
                        rs.getString("img_src")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Add item to marketplace catalog (admin/system function)
     */
    public boolean addItemToCatalog(String itemName, double price, String description, String imgSrc) {
        String sql = "INSERT INTO WISHLIST (item_name, item_price, description, img_src) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, itemName);
            pstmt.setDouble(2, price);
            pstmt.setString(3, description);
            pstmt.setString(4, imgSrc);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get user's personal wishlist (items they selected from marketplace)
     */
    public List<Item> getUserWishlist(int userId) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT w.item_id, w.item_name, w.item_price, w.description, w.img_src, " +
                "uw.created_date, uw.wish_id, COALESCE(SUM(c.amount), 0) AS funded " +
                "FROM WISHLIST w " +
                "JOIN USERWISHES uw ON w.item_id = uw.item_id " +
                "LEFT JOIN CONTRIBUTIONS c ON uw.wish_id = c.wish_id " +
                "WHERE uw.user_id = ? " +
                "GROUP BY w.item_id, w.item_name, w.item_price, w.description, w.img_src, uw.created_date, uw.wish_id " +
                "ORDER BY uw.created_date DESC";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Item item = new Item(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getDouble("item_price"),
                        rs.getString("img_src"));
                item.setDescription(rs.getString("description"));
                item.setFunded(rs.getDouble("funded"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Add item from marketplace to user's wishlist
     */
    public boolean addToUserWishlist(int userId, int itemId) {
        // Check if already in wishlist
        String checkSql = "SELECT wish_id FROM USERWISHES WHERE user_id = ? AND item_id = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, itemId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false; // Already in wishlist
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Add to wishlist
        String sql = "INSERT INTO USERWISHES (user_id, item_id) VALUES (?, ?)";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove item from user's wishlist
     */
    public boolean removeFromUserWishlist(int userId, int itemId) {
        String sql = "DELETE FROM USERWISHES WHERE user_id = ? AND item_id = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get total contributions for a user's wishlist item
     */
    public double getTotalContributions(int wishId) {
        String sql = "SELECT SUM(amount) as total FROM CONTRIBUTIONS WHERE wish_id = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, wishId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int findWishIdForUserItem(int ownerUserId, int itemId) {
        String sql = "SELECT wish_id FROM USERWISHES WHERE user_id = ? AND item_id = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ownerUserId);
            pstmt.setInt(2, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("wish_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
