package com.library.dao;

import com.library.config.DBConnection;
import com.library.model.LibraryTransaction;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionDAO {

    /**
     * Issues a book using an atomic ACID transaction.
     * Decreases available book copies and creates the transaction record.
     */
    public boolean issueBook(LibraryTransaction trans, String adminUsername) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Check and decrement book availability
            String updateBookSql = "UPDATE books SET available_copies = available_copies - 1 " +
                                  "WHERE book_id = ? AND available_copies > 0";
            try (PreparedStatement stmt = conn.prepareStatement(updateBookSql)) {
                stmt.setInt(1, trans.getBookId());
                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    throw new SQLException("Cannot issue book: No available copies remaining for this book.");
                }
            }

            // 2. Insert transaction record
            String insertSql = "INSERT INTO transactions (book_id, member_id, issue_date, due_date, status, notes) " +
                              "VALUES (?, ?, ?, ?, 'ISSUED', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, trans.getBookId());
                stmt.setInt(2, trans.getMemberId());
                stmt.setDate(3, trans.getIssueDate());
                stmt.setDate(4, trans.getDueDate());
                stmt.setString(5, trans.getNotes());

                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    throw new SQLException("Failed to create transaction record.");
                }

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        trans.setTransId(rs.getInt(1));
                    }
                }
            }

            // 3. Log activity
            String logSql = "INSERT INTO activity_logs (admin_username, action_type, description) VALUES (?, 'ISSUE_BOOK', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(logSql)) {
                stmt.setString(1, adminUsername != null ? adminUsername : "admin");
                stmt.setString(2, "Issued book ID #" + trans.getBookId() + " to Member ID #" + trans.getMemberId());
                stmt.executeUpdate();
            }

            conn.commit(); // Transaction success!
            return true;

        } catch (SQLException e) {
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

    /**
     * Returns a book using an atomic ACID transaction.
     * Restores available book copies, marks transaction RETURNED, and records fine payment if applicable.
     */
    public boolean returnBook(int transId, Date returnDate, double fineAmount, Double paymentAmount,
                              String paymentMethod, String adminUsername) throws SQLException {
        if (fineAmount < 0 || !Double.isFinite(fineAmount)
                || (paymentAmount != null && (paymentAmount < 0 || !Double.isFinite(paymentAmount)))) {
            throw new IllegalArgumentException("Fine and payment amounts must be valid non-negative amounts.");
        }
        if (paymentAmount != null && paymentAmount > fineAmount + 0.005) {
            throw new IllegalArgumentException("Payment cannot exceed the fine due.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Fetch transaction and verify status
            int bookId;
            int memberId;
            String checkSql = "SELECT book_id, member_id, status FROM transactions WHERE trans_id = ? FOR UPDATE";
            try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                stmt.setInt(1, transId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        throw new SQLException("Transaction #" + transId + " not found.");
                    }
                    if (!"ISSUED".equalsIgnoreCase(rs.getString("status"))) {
                        conn.rollback();
                        throw new SQLException("This transaction is not an active loan.");
                    }
                    bookId = rs.getInt("book_id");
                    memberId = rs.getInt("member_id");
                }
            }

            // 2. Increment book available copies
            String updateBookSql = "UPDATE books SET available_copies = available_copies + 1 " +
                                  "WHERE book_id = ? AND available_copies < total_copies";
            try (PreparedStatement stmt = conn.prepareStatement(updateBookSql)) {
                stmt.setInt(1, bookId);
                if (stmt.executeUpdate() != 1) {
                    throw new SQLException("Could not restore book availability; the inventory count is inconsistent.");
                }
            }

            // 3. Mark transaction as RETURNED
            String updateTransSql = "UPDATE transactions SET return_date = ?, status = 'RETURNED' " +
                                    "WHERE trans_id = ? AND status = 'ISSUED'";
            try (PreparedStatement stmt = conn.prepareStatement(updateTransSql)) {
                stmt.setDate(1, returnDate);
                stmt.setInt(2, transId);
                if (stmt.executeUpdate() != 1) {
                    throw new SQLException("Could not mark the loan as returned.");
                }
            }

            // 4. Record fine payment if payment amount is provided and > 0
            if (paymentAmount != null && paymentAmount > 0) {
                String fineSql = "INSERT INTO fine_payments (trans_id, member_id, amount, payment_date, payment_method, notes) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(fineSql)) {
                    stmt.setInt(1, transId);
                    stmt.setInt(2, memberId);
                    stmt.setDouble(3, paymentAmount);
                    stmt.setDate(4, returnDate);
                    stmt.setString(5, paymentMethod != null ? paymentMethod : "Cash");
                    stmt.setString(6, "Settled upon book return");
                    stmt.executeUpdate();
                }
            }

            // 5. Activity log
            String logSql = "INSERT INTO activity_logs (admin_username, action_type, description) VALUES (?, 'RETURN_BOOK', ?)";
            try (PreparedStatement stmt = conn.prepareStatement(logSql)) {
                stmt.setString(1, adminUsername != null ? adminUsername : "admin");
                stmt.setString(2, "Returned book ID #" + bookId + " from Member ID #" + memberId +
                        (paymentAmount != null && paymentAmount > 0 ? " (Fine paid: " + paymentAmount + ")" : ""));
                stmt.executeUpdate();
            }

            conn.commit(); // Transaction complete!
            return true;

        } catch (SQLException e) {
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

    public List<LibraryTransaction> getAllTransactions() throws SQLException {
        return searchTransactions(null, null);
    }

    public List<LibraryTransaction> searchTransactions(String keyword, String statusFilter) throws SQLException {
        List<LibraryTransaction> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.trans_id, t.book_id, t.member_id, t.issue_date, t.due_date, t.return_date, " +
                "t.status, t.notes, t.created_at, " +
                "b.title AS book_title, b.isbn AS book_isbn, " +
                "m.name AS member_name, m.member_code, m.email AS member_email, " +
                "COALESCE((SELECT SUM(amount) FROM fine_payments fp WHERE fp.trans_id = t.trans_id), 0.0) AS fine_paid " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.book_id " +
                "JOIN members m ON t.member_id = m.member_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(b.title) LIKE ? OR LOWER(m.name) LIKE ? OR LOWER(m.member_code) LIKE ? OR b.isbn LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (statusFilter != null && !statusFilter.equals("All") && !statusFilter.isEmpty()) {
            sql.append("AND t.status = ? ");
            params.add(statusFilter);
        }

        sql.append("ORDER BY t.trans_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LibraryTransaction t = new LibraryTransaction(
                            rs.getInt("trans_id"),
                            rs.getInt("book_id"),
                            rs.getInt("member_id"),
                            rs.getDate("issue_date"),
                            rs.getDate("due_date"),
                            rs.getDate("return_date"),
                            rs.getString("status")
                    );
                    t.setNotes(rs.getString("notes"));
                    t.setCreatedAt(rs.getTimestamp("created_at"));
                    t.setBookTitle(rs.getString("book_title"));
                    t.setBookIsbn(rs.getString("book_isbn"));
                    t.setMemberName(rs.getString("member_name"));
                    t.setMemberCode(rs.getString("member_code"));
                    t.setMemberEmail(rs.getString("member_email"));
                    t.setFinePaid(rs.getDouble("fine_paid"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    public List<LibraryTransaction> getActiveLoans() throws SQLException {
        return searchTransactions(null, "ISSUED");
    }

    public List<LibraryTransaction> getTransactionsByMember(int memberId) throws SQLException {
        List<LibraryTransaction> list = new ArrayList<>();
        String sql = "SELECT t.trans_id, t.book_id, t.member_id, t.issue_date, t.due_date, t.return_date, " +
                     "t.status, t.notes, t.created_at, " +
                     "b.title AS book_title, b.isbn AS book_isbn, " +
                     "m.name AS member_name, m.member_code, m.email AS member_email, " +
                     "COALESCE((SELECT SUM(amount) FROM fine_payments fp WHERE fp.trans_id = t.trans_id), 0.0) AS fine_paid " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN members m ON t.member_id = m.member_id " +
                     "WHERE t.member_id = ? ORDER BY t.trans_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LibraryTransaction t = new LibraryTransaction(
                            rs.getInt("trans_id"),
                            rs.getInt("book_id"),
                            rs.getInt("member_id"),
                            rs.getDate("issue_date"),
                            rs.getDate("due_date"),
                            rs.getDate("return_date"),
                            rs.getString("status")
                    );
                    t.setNotes(rs.getString("notes"));
                    t.setCreatedAt(rs.getTimestamp("created_at"));
                    t.setBookTitle(rs.getString("book_title"));
                    t.setBookIsbn(rs.getString("book_isbn"));
                    t.setMemberName(rs.getString("member_name"));
                    t.setMemberCode(rs.getString("member_code"));
                    t.setMemberEmail(rs.getString("member_email"));
                    t.setFinePaid(rs.getDouble("fine_paid"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    public int getBooksIssuedCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status = 'ISSUED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getPendingReturnsCount() throws SQLException {
        // Books that are currently issued and due within the next 3 days or already overdue
        String sql = "SELECT COUNT(*) FROM transactions " +
                     "WHERE status = 'ISSUED' AND due_date <= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now().plusDays(3)));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getOverdueBooksCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status = 'ISSUED' AND due_date < CURRENT_DATE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Retrieves daily counts of issues and returns for the last 7 days for the dashboard chart.
     */
    public Map<String, int[]> getWeeklyIssueReturnCounts() throws SQLException {
        Map<String, int[]> map = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.minusDays(6);
        for (LocalDate day = firstDay; !day.isAfter(today); day = day.plusDays(1)) {
            String dayName = day.getDayOfWeek().toString().substring(0, 3);
            dayName = dayName.substring(0, 1) + dayName.substring(1).toLowerCase();
            map.put(dayName, new int[]{0, 0});
        }

        String sqlIssues = "SELECT issue_date, COUNT(*) AS cnt FROM transactions " +
                           "WHERE issue_date BETWEEN ? AND ? GROUP BY issue_date";
        String sqlReturns = "SELECT return_date, COUNT(*) AS cnt FROM transactions " +
                            "WHERE return_date BETWEEN ? AND ? GROUP BY return_date";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlIssues)) {
                stmt.setDate(1, Date.valueOf(firstDay));
                stmt.setDate(2, Date.valueOf(today));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Date dt = rs.getDate("issue_date");
                        if (dt != null) {
                            String dayName = dt.toLocalDate().getDayOfWeek().toString().substring(0, 3);
                            dayName = dayName.substring(0, 1) + dayName.substring(1).toLowerCase();
                            int[] counts = map.get(dayName);
                            if (counts != null) {
                                counts[0] += rs.getInt("cnt");
                            }
                        }
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlReturns)) {
                stmt.setDate(1, Date.valueOf(firstDay));
                stmt.setDate(2, Date.valueOf(today));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Date dt = rs.getDate("return_date");
                        if (dt != null) {
                            String dayName = dt.toLocalDate().getDayOfWeek().toString().substring(0, 3);
                            dayName = dayName.substring(0, 1) + dayName.substring(1).toLowerCase();
                            int[] counts = map.get(dayName);
                            if (counts != null) {
                                counts[1] += rs.getInt("cnt");
                            }
                        }
                    }
                }
            }
        }
        return map;
    }
}
