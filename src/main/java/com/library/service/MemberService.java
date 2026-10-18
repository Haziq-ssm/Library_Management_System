package com.library.service;

import com.library.dao.MemberDAO;
import com.library.dao.TransactionDAO;
import com.library.model.LibraryTransaction;
import com.library.model.Member;
import com.library.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class MemberService {

    private final MemberDAO memberDAO;
    private final TransactionDAO transactionDAO;

    public MemberService() {
        this.memberDAO = new MemberDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public List<Member> getAllMembers() throws SQLException {
        return memberDAO.getAllMembers();
    }

    public List<Member> searchAndFilter(String keyword, String typeFilter, String statusFilter) throws SQLException {
        return memberDAO.searchAndFilter(keyword, typeFilter, statusFilter);
    }

    public Member getMemberById(int id) throws SQLException {
        return memberDAO.getMemberById(id);
    }

    public Member getMemberByCode(String code) throws SQLException {
        return memberDAO.getMemberByCode(code);
    }

    public boolean saveMember(Member member) throws SQLException {
        validateMember(member);

        if (member.getMemberId() > 0) {
            return memberDAO.updateMember(member);
        } else {
            if (!ValidationUtil.isNotEmpty(member.getMemberCode())) {
                member.setMemberCode(memberDAO.generateNextMemberCode());
            }
            if (member.getRegistrationDate() == null) {
                member.setRegistrationDate(new java.sql.Date(System.currentTimeMillis()));
            }
            if (!ValidationUtil.isNotEmpty(member.getStatus())) {
                member.setStatus("Active");
            }
            return memberDAO.addMember(member);
        }
    }

    public boolean toggleStatus(int memberId, String currentStatus) throws SQLException {
        String newStatus = "Active".equalsIgnoreCase(currentStatus) ? "Inactive" : "Active";
        return memberDAO.toggleMemberStatus(memberId, newStatus);
    }

    public boolean deleteMember(int memberId) throws SQLException {
        return memberDAO.deleteMember(memberId);
    }

    public List<LibraryTransaction> getMemberBorrowingHistory(int memberId) throws SQLException {
        return transactionDAO.getTransactionsByMember(memberId);
    }

    private void validateMember(Member member) throws SQLException {
        if (!ValidationUtil.isNotEmpty(member.getName())) {
            throw new IllegalArgumentException("Member name is required.");
        }
        if (!ValidationUtil.isValidEmail(member.getEmail())) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (memberDAO.isEmailExists(member.getEmail(), member.getMemberId() > 0 ? member.getMemberId() : null)) {
            throw new IllegalArgumentException("A member with email '" + member.getEmail() + "' already exists.");
        }
        if (ValidationUtil.isNotEmpty(member.getPhone()) && !ValidationUtil.isValidPhone(member.getPhone())) {
            throw new IllegalArgumentException("Phone number format is invalid (7-15 digits allowed).");
        }
    }
}
