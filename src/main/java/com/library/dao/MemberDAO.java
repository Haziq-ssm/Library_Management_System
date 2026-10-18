package com.library.dao;

import com.library.config.DBConnection;
import com.library.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public List<Member> getAllMembers() throws SQLException {
        return searchAndFilter(null, null, null);
    }

    public List<Member> searchAndFilter(String keyword, String typeFilter, String statusFilter) throws SQLException {
        List<Member> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT m.member_id, m.member_code, m.name, m.email, m.phone, m.membership_type, " +
                "m.registration_date, m.status, m.created_at, " +
                "(SELECT COUNT(*) FROM transactions t WHERE t.member_id = m.member_id AND t.status = 'ISSUED') AS active_loans " +
                "FROM members m " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(m.name) LIKE ? OR LOWER(m.email) LIKE ? OR LOWER(m.member_code) LIKE ? OR m.phone LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (typeFilter != null && !typeFilter.equals("All Members") && !typeFilter.isEmpty()) {
            sql.append("AND m.membership_type = ? ");
            params.add(typeFilter);
        }

        if (statusFilter != null && !statusFilter.isEmpty()) {
            sql.append("AND m.status = ? ");
            params.add(statusFilter);
        }

        sql.append("ORDER BY m.member_id ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Member m = new Member(
                            rs.getInt("member_id"),
                            rs.getString("member_code"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("membership_type"),
                            rs.getDate("registration_date"),
                            rs.getString("status")
                    );
                    m.setCreatedAt(rs.getTimestamp("created_at"));
                    m.setActiveLoansCount(rs.getInt("active_loans"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    public Member getMemberById(int memberId) throws SQLException {
        String sql = "SELECT m.member_id, m.member_code, m.name, m.email, m.phone, m.membership_type, " +
                     "m.registration_date, m.status, m.created_at, " +
                     "(SELECT COUNT(*) FROM transactions t WHERE t.member_id = m.member_id AND t.status = 'ISSUED') AS active_loans " +
                     "FROM members m WHERE m.member_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Member m = new Member(
                            rs.getInt("member_id"),
                            rs.getString("member_code"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("membership_type"),
                            rs.getDate("registration_date"),
                            rs.getString("status")
                    );
                    m.setCreatedAt(rs.getTimestamp("created_at"));
                    m.setActiveLoansCount(rs.getInt("active_loans"));
                    return m;
                }
            }
        }
        return null;
    }

    public Member getMemberByCode(String code) throws SQLException {
        String sql = "SELECT m.member_id, m.member_code, m.name, m.email, m.phone, m.membership_type, " +
                     "m.registration_date, m.status, m.created_at, " +
                     "(SELECT COUNT(*) FROM transactions t WHERE t.member_id = m.member_id AND t.status = 'ISSUED') AS active_loans " +
                     "FROM members m WHERE m.member_code = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Member m = new Member(
                            rs.getInt("member_id"),
                            rs.getString("member_code"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("membership_type"),
                            rs.getDate("registration_date"),
                            rs.getString("status")
                    );
                    m.setCreatedAt(rs.getTimestamp("created_at"));
                    m.setActiveLoansCount(rs.getInt("active_loans"));
                    return m;
                }
            }
        }
        return null;
    }

    public boolean addMember(Member member) throws SQLException {
        String sql = "INSERT INTO members (member_code, name, email, phone, membership_type, registration_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, member.getMemberCode());
            stmt.setString(2, member.getName());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getPhone());
            stmt.setString(5, member.getMembershipType());
            stmt.setDate(6, member.getRegistrationDate());
            stmt.setString(7, member.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        member.setMemberId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateMember(Member member) throws SQLException {
        String sql = "UPDATE members SET name = ?, email = ?, phone = ?, membership_type = ?, status = ? " +
                     "WHERE member_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getMembershipType());
            stmt.setString(5, member.getStatus());
            stmt.setInt(6, member.getMemberId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean toggleMemberStatus(int memberId, String newStatus) throws SQLException {
        String sql = "UPDATE members SET status = ? WHERE member_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, memberId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteMember(int memberId) throws SQLException {
        // Enforce business rule: Check active loans
        String checkLoans = "SELECT COUNT(*) FROM transactions WHERE member_id = ? AND status = 'ISSUED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkLoans)) {

            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete member: The member currently has unreturned borrowed books.");
                }
            }
        }

        String sql = "DELETE FROM members WHERE member_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean isEmailExists(String email, Integer excludeMemberId) throws SQLException {
        if (email == null || email.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM members WHERE LOWER(email) = ?" +
                     (excludeMemberId != null && excludeMemberId > 0 ? " AND member_id <> ?" : "");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim().toLowerCase());
            if (excludeMemberId != null && excludeMemberId > 0) {
                stmt.setInt(2, excludeMemberId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int getActiveMembersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM members WHERE status = 'Active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public String generateNextMemberCode() throws SQLException {
        String sql = "SELECT MAX(member_id) FROM members";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int next = 1001;
            if (rs.next()) {
                int maxId = rs.getInt(1);
                next = 1000 + maxId + 1;
            }
            return "M" + next;
        }
    }
}
