package com.iwish.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}