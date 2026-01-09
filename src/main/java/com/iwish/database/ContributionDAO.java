package com.iwish.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContributionDAO {

    private final DatabaseManager dbManager;

    public ContributionDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean addContribution(int wishId, int contributorId, double amount) {
        String sql = "INSERT INTO CONTRIBUTIONS (wish_id, contributor_id, amount) VALUES (?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, wishId);
            pstmt.setInt(2, contributorId);
            pstmt.setDouble(3, amount);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all distinct contributors for a specific wish item.
     * Used to notify contributors when item reaches 100% funding.
     *
     * @param wishId The wish ID to query contributors for
     * @return List of contributor user IDs
     * @throws SQLException if database access fails
     */
    public List<Integer> getContributorsForWish(int wishId) throws SQLException {
        List<Integer> contributors = new ArrayList<>();
        String sql = "SELECT DISTINCT contributor_id FROM Contributions WHERE wish_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, wishId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    contributors.add(rs.getInt("contributor_id"));
                }
            }
        }
        return contributors;
    }
}