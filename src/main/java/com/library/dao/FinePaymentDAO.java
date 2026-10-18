package com.library.dao;

import com.library.config.DBConnection;
import com.library.model.FinePayment;
import com.library.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FinePaymentDAO {

    public boolean recordPayment(FinePayment payment, String adminUsername, double fineRatePerDay) throws SQLException {
        if (!Double.isFinite(payment.getAmount()) || payment.getAmount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be a valid value greater than zero.");
        }
        if (!Double.isFinite(fineRatePerDay) || fineRatePerDay < 0) {
            throw new IllegalArgumentException("The configured fine rate is invalid.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String transactionSql = "SELECT member_id, due_date, return_date, status " +
                                    "FROM transactions WHERE trans_id = ? FOR UPDATE";
            java.sql.Date dueDate;
            java.sql.Date returnDate;
            boolean returned;
            try (PreparedStatement stmt = conn.prepareStatement(transactionSql)) {
                stmt.setInt(1, payment.getTransId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("The loan for this fine payment could not be found.");
                    }
                    if (rs.getInt("member_id") != payment.getMemberId()) {
                        throw new IllegalArgumentException("The selected member does not match this loan.");
                    }
                    dueDate = rs.getDate("due_date");
                    returnDate = rs.getDate("return_date");
                    returned = "RETURNED".equalsIgnoreCase(rs.getString("status"));
                }
            }

            java.sql.Date effectiveReturnDate = returned ? returnDate : DateUtil.today();
            double totalFine = DateUtil.calculateOverdueDays(dueDate, effectiveReturnDate) * fineRatePerDay;
            String paidSql = "SELECT COALESCE(SUM(amount), 0.0) FROM fine_payments WHERE trans_id = ?";
            double paid;
            try (PreparedStatement stmt = conn.prepareStatement(paidSql)) {
                stmt.setInt(1, payment.getTransId());
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    paid = rs.getDouble(1);
                }
            }
            double balance = Math.max(0.0, totalFine - paid);
            if (payment.getAmount() > balance + 0.005) {
                throw new IllegalArgumentException(
                        String.format("Payment exceeds the outstanding fine balance of ₹%.2f.", balance));
            }

            String sql = "INSERT INTO fine_payments (trans_id, member_id, amount, payment_date, payment_method, notes) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, payment.getTransId());
                stmt.setInt(2, payment.getMemberId());
                stmt.setDouble(3, payment.getAmount());
                stmt.setDate(4, payment.getPaymentDate());
                stmt.setString(5, payment.getPaymentMethod());
                stmt.setString(6, payment.getNotes());

                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        payment.setPaymentId(rs.getInt(1));
                    }
                }
            }

            // Log activity
            String logSql = "INSERT INTO activity_logs (admin_username, action_type, description) VALUES (?, 'FINE_PAYMENT', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(logSql)) {
                stmt.setString(1, adminUsername != null ? adminUsername : "admin");
                stmt.setString(2, "Received fine payment of ₹" + payment.getAmount() + " from Member ID #" + payment.getMemberId());
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public List<FinePayment> getAllPayments() throws SQLException {
        List<FinePayment> list = new ArrayList<>();
        String sql = "SELECT fp.payment_id, fp.trans_id, fp.member_id, fp.amount, fp.payment_date, " +
                     "fp.payment_method, fp.notes, fp.created_at, " +
                     "m.name AS member_name, m.member_code, b.title AS book_title " +
                     "FROM fine_payments fp " +
                     "JOIN members m ON fp.member_id = m.member_id " +
                     "JOIN transactions t ON fp.trans_id = t.trans_id " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "ORDER BY fp.payment_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                FinePayment fp = new FinePayment(
                        rs.getInt("payment_id"),
                        rs.getInt("trans_id"),
                        rs.getInt("member_id"),
                        rs.getDouble("amount"),
                        rs.getDate("payment_date"),
                        rs.getString("payment_method"),
                        rs.getString("notes")
                );
                fp.setCreatedAt(rs.getTimestamp("created_at"));
                fp.setMemberName(rs.getString("member_name"));
                fp.setMemberCode(rs.getString("member_code"));
                fp.setBookTitle(rs.getString("book_title"));
                list.add(fp);
            }
        }
        return list;
    }

    public double getTotalFinesCollected() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM fine_payments";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    public double getTotalOutstandingFines(double fineRatePerDay) throws SQLException {
        // Outstanding fines on currently overdue loans and returned loans with unpaid balances
        double outstanding = 0.0;
        String sql = "SELECT t.due_date, t.return_date, " +
                     "COALESCE((SELECT SUM(amount) FROM fine_payments fp WHERE fp.trans_id = t.trans_id), 0.0) AS paid " +
                     "FROM transactions t " +
                     "WHERE (t.status = 'ISSUED' AND t.due_date < CURRENT_DATE) " +
                     "   OR (t.status = 'RETURNED' AND t.return_date > t.due_date)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                java.sql.Date due = rs.getDate("due_date");
                java.sql.Date ret = rs.getDate("return_date");
                double paid = rs.getDouble("paid");

                int overdueDays = com.library.util.DateUtil.calculateOverdueDays(due, ret);
                double totalFine = overdueDays * fineRatePerDay;
                double balance = Math.max(0.0, totalFine - paid);
                outstanding += balance;
            }
        }
        return outstanding;
    }
}
