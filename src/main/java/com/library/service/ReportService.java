package com.library.service;

import com.library.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class ReportService {

    public DefaultTableModel getBookInventoryReport() throws SQLException {
        String sql = "SELECT b.book_id AS 'Book ID', b.title AS 'Title', b.author AS 'Author', " +
                     "c.category_name AS 'Category', b.isbn AS 'ISBN', b.total_copies AS 'Total Copies', " +
                     "b.available_copies AS 'Available', (b.total_copies - b.available_copies) AS 'Issued Copies' " +
                     "FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "ORDER BY b.title ASC";
        return buildTableModel(sql);
    }

    public DefaultTableModel getCurrentlyIssuedReport() throws SQLException {
        String sql = "SELECT t.trans_id AS 'Trans ID', b.title AS 'Book Title', m.name AS 'Member Name', " +
                     "m.member_code AS 'Member ID', t.issue_date AS 'Issue Date', t.due_date AS 'Due Date', " +
                     "DATEDIFF(CURRENT_DATE, t.due_date) AS 'Days Overdue' " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN members m ON t.member_id = m.member_id " +
                     "WHERE t.status = 'ISSUED' " +
                     "ORDER BY t.due_date ASC";
        return buildTableModel(sql);
    }

    public DefaultTableModel getOverdueBooksReport() throws SQLException {
        String sql = "SELECT t.trans_id AS 'Trans ID', b.title AS 'Book Title', m.name AS 'Member Name', " +
                     "m.email AS 'Email', m.phone AS 'Phone', t.issue_date AS 'Issue Date', " +
                     "t.due_date AS 'Due Date', DATEDIFF(CURRENT_DATE, t.due_date) AS 'Overdue Days', " +
                     "(DATEDIFF(CURRENT_DATE, t.due_date) * 5.0) AS 'Estimated Fine (₹)' " +
                     "FROM transactions t " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "JOIN members m ON t.member_id = m.member_id " +
                     "WHERE t.status = 'ISSUED' AND t.due_date < CURRENT_DATE " +
                     "ORDER BY t.due_date ASC";
        return buildTableModel(sql);
    }

    public DefaultTableModel getMostPopularBooksReport() throws SQLException {
        String sql = "SELECT b.title AS 'Book Title', b.author AS 'Author', c.category_name AS 'Category', " +
                     "COUNT(t.trans_id) AS 'Times Borrowed', b.available_copies AS 'Current Available' " +
                     "FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "LEFT JOIN transactions t ON b.book_id = t.book_id " +
                     "GROUP BY b.book_id, b.title, b.author, c.category_name, b.available_copies " +
                     "ORDER BY COUNT(t.trans_id) DESC, b.title ASC";
        return buildTableModel(sql);
    }

    public DefaultTableModel getFineCollectionReport() throws SQLException {
        String sql = "SELECT fp.payment_id AS 'Receipt ID', m.name AS 'Member Name', m.member_code AS 'Member ID', " +
                     "b.title AS 'Book Title', fp.amount AS 'Amount (₹)', fp.payment_date AS 'Payment Date', " +
                     "fp.payment_method AS 'Method', fp.notes AS 'Notes' " +
                     "FROM fine_payments fp " +
                     "JOIN members m ON fp.member_id = m.member_id " +
                     "JOIN transactions t ON fp.trans_id = t.trans_id " +
                     "JOIN books b ON t.book_id = b.book_id " +
                     "ORDER BY fp.payment_date DESC, fp.payment_id DESC";
        return buildTableModel(sql);
    }

    private DefaultTableModel buildTableModel(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            Vector<String> columnNames = new Vector<>();
            for (int col = 1; col <= columnCount; col++) {
                columnNames.add(metaData.getColumnLabel(col));
            }

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> vector = new Vector<>();
                for (int col = 1; col <= columnCount; col++) {
                    vector.add(rs.getObject(col));
                }
                data.add(vector);
            }

            return new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
        }
    }
}
