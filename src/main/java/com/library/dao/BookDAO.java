package com.library.dao;

import com.library.config.DBConnection;
import com.library.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public List<Book> getAllBooks() throws SQLException {
        return searchAndFilter(null, null, "All Books", null);
    }

    public List<Book> searchAndFilter(String keyword, Integer categoryId, String statusFilter, String sortBy) throws SQLException {
        List<Book> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT b.book_id, b.title, b.author, b.publisher, b.isbn, b.category_id, " +
                "c.category_name, b.description, b.total_copies, b.available_copies, " +
                "b.publication_year, b.cover_image, b.created_at " +
                "FROM books b " +
                "LEFT JOIN categories c ON b.category_id = c.category_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(b.title) LIKE ? OR LOWER(b.author) LIKE ? OR LOWER(b.publisher) LIKE ? OR LOWER(b.isbn) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append("AND b.category_id = ? ");
            params.add(categoryId);
        }

        if (statusFilter != null && !statusFilter.equals("All Books") && !statusFilter.isEmpty()) {
            if ("Available".equalsIgnoreCase(statusFilter)) {
                sql.append("AND b.available_copies > 0 ");
            } else if ("Issued".equalsIgnoreCase(statusFilter)) {
                sql.append("AND b.available_copies = 0 ");
            } else if ("Lost/Damaged".equalsIgnoreCase(statusFilter) || "Low Stock".equalsIgnoreCase(statusFilter)) {
                sql.append("AND b.available_copies <= 1 ");
            }
        }

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            if ("Title (A-Z)".equalsIgnoreCase(sortBy)) {
                sql.append("ORDER BY b.title ASC ");
            } else if ("Title (Z-A)".equalsIgnoreCase(sortBy)) {
                sql.append("ORDER BY b.title DESC ");
            } else if ("Author".equalsIgnoreCase(sortBy)) {
                sql.append("ORDER BY b.author ASC ");
            } else if ("Available Copies".equalsIgnoreCase(sortBy)) {
                sql.append("ORDER BY b.available_copies DESC ");
            } else {
                sql.append("ORDER BY b.book_id DESC ");
            }
        } else {
            sql.append("ORDER BY b.book_id ASC ");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book b = new Book(
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("publisher"),
                            rs.getString("isbn"),
                            rs.getInt("category_id"),
                            rs.getString("category_name"),
                            rs.getString("description"),
                            rs.getInt("total_copies"),
                            rs.getInt("available_copies"),
                            (Integer) rs.getObject("publication_year")
                    );
                    b.setCoverImage(rs.getString("cover_image"));
                    b.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(b);
                }
            }
        }
        return list;
    }

    public Book getBookById(int bookId) throws SQLException {
        String sql = "SELECT b.book_id, b.title, b.author, b.publisher, b.isbn, b.category_id, " +
                     "c.category_name, b.description, b.total_copies, b.available_copies, " +
                     "b.publication_year, b.cover_image, b.created_at " +
                     "FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "WHERE b.book_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Book b = new Book(
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("publisher"),
                            rs.getString("isbn"),
                            rs.getInt("category_id"),
                            rs.getString("category_name"),
                            rs.getString("description"),
                            rs.getInt("total_copies"),
                            rs.getInt("available_copies"),
                            (Integer) rs.getObject("publication_year")
                    );
                    b.setCoverImage(rs.getString("cover_image"));
                    b.setCreatedAt(rs.getTimestamp("created_at"));
                    return b;
                }
            }
        }
        return null;
    }

    public boolean addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, publisher, isbn, category_id, description, " +
                     "total_copies, available_copies, publication_year, cover_image) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getPublisher());
            stmt.setString(4, book.getIsbn());

            if (book.getCategoryId() > 0) {
                stmt.setInt(5, book.getCategoryId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, book.getDescription());
            stmt.setInt(7, book.getTotalCopies());
            stmt.setInt(8, book.getAvailableCopies());

            if (book.getPublicationYear() != null) {
                stmt.setInt(9, book.getPublicationYear());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }

            stmt.setString(10, book.getCoverImage());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        book.setBookId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, publisher = ?, isbn = ?, category_id = ?, " +
                     "description = ?, total_copies = ?, available_copies = ?, publication_year = ?, cover_image = ? " +
                     "WHERE book_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getPublisher());
            stmt.setString(4, book.getIsbn());

            if (book.getCategoryId() > 0) {
                stmt.setInt(5, book.getCategoryId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, book.getDescription());
            stmt.setInt(7, book.getTotalCopies());
            stmt.setInt(8, book.getAvailableCopies());

            if (book.getPublicationYear() != null) {
                stmt.setInt(9, book.getPublicationYear());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }

            stmt.setString(10, book.getCoverImage());
            stmt.setInt(11, book.getBookId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteBook(int bookId) throws SQLException {
        // Enforce business rule: Cannot delete book if active loans exist
        String checkSql = "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND status = 'ISSUED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, bookId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete book: There are active loans currently issued to members for this book.");
                }
            }
        }

        // Delete associated returned transactions or delete book
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean isIsbnExists(String isbn, Integer excludeBookId) throws SQLException {
        if (isbn == null || isbn.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM books WHERE isbn = ?" +
                     (excludeBookId != null && excludeBookId > 0 ? " AND book_id <> ?" : "");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn.trim());
            if (excludeBookId != null && excludeBookId > 0) {
                stmt.setInt(2, excludeBookId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Updates available copies inside an ongoing transaction.
     */
    public boolean changeAvailableCopies(Connection conn, int bookId, int delta) throws SQLException {
        String sql = "UPDATE books SET available_copies = available_copies + ? " +
                     "WHERE book_id = ? AND (available_copies + ?) >= 0 AND (available_copies + ?) <= total_copies";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, delta);
            stmt.setInt(2, bookId);
            stmt.setInt(3, delta);
            stmt.setInt(4, delta);
            return stmt.executeUpdate() > 0;
        }
    }

    public int getTotalBooksCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getTotalAvailableCopiesCount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(available_copies), 0) FROM books";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
