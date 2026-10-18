package com.library.service;

import com.library.dao.*;
import com.library.model.*;
import com.library.util.DateUtil;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class LibraryService {

    private final TransactionDAO transactionDAO;
    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;
    private final FinePaymentDAO finePaymentDAO;
    private final SettingDAO settingDAO;
    private final ActivityLogDAO activityLogDAO;

    public LibraryService() {
        this.transactionDAO = new TransactionDAO();
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
        this.finePaymentDAO = new FinePaymentDAO();
        this.settingDAO = new SettingDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }

    public boolean issueBook(int bookId, int memberId, Date issueDate, Date dueDate, String notes, String adminUsername) throws SQLException {
        // 1. Verify Member
        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Selected member does not exist.");
        }
        if (!member.isActive()) {
            throw new IllegalArgumentException("Cannot issue book: Member account is currently INACTIVE.");
        }

        int maxBooksAllowed = settingDAO.getIntSetting("max_books_per_member", 5);
        if (member.getActiveLoansCount() >= maxBooksAllowed) {
            throw new IllegalArgumentException("Borrowing limit reached: Member already has " +
                    member.getActiveLoansCount() + " books issued (Maximum allowed: " + maxBooksAllowed + ").");
        }

        // 2. Verify Book
        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Selected book does not exist.");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalArgumentException("No copies available for '" + book.getTitle() + "'.");
        }

        if (issueDate == null) {
            issueDate = DateUtil.today();
        }
        if (dueDate == null) {
            int loanDays = settingDAO.getIntSetting("loan_period_days", 14);
            dueDate = DateUtil.addDays(issueDate, loanDays);
        }
        if (!dueDate.after(issueDate)) {
            throw new IllegalArgumentException("The due date must be after the issue date.");
        }

        LibraryTransaction trans = new LibraryTransaction();
        trans.setBookId(bookId);
        trans.setMemberId(memberId);
        trans.setIssueDate(issueDate);
        trans.setDueDate(dueDate);
        trans.setNotes(notes);

        return transactionDAO.issueBook(trans, adminUsername);
    }

    public boolean returnBook(int transId, Date returnDate, Double paymentAmount, String paymentMethod, String adminUsername) throws SQLException {
        if (returnDate == null) {
            returnDate = DateUtil.today();
        }

        // Calculate fine for this transaction
        List<LibraryTransaction> transactions = transactionDAO.searchTransactions(null, "ISSUED");
        LibraryTransaction target = null;
        for (LibraryTransaction t : transactions) {
            if (t.getTransId() == transId) {
                target = t;
                break;
            }
        }

        if (target == null) {
            throw new IllegalArgumentException("Active loan transaction #" + transId + " not found or already returned.");
        }
        if (target.getIssueDate() != null && returnDate.before(target.getIssueDate())) {
            throw new IllegalArgumentException("The return date cannot be before the issue date.");
        }

        double fineRate = settingDAO.getDoubleSetting("fine_per_day", 5.00);
        int overdueDays = DateUtil.calculateOverdueDays(target.getDueDate(), returnDate);
        double fineAmount = overdueDays * fineRate;

        return transactionDAO.returnBook(transId, returnDate, fineAmount, paymentAmount, paymentMethod, adminUsername);
    }

    public DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();
        stats.setTotalBooks(bookDAO.getTotalBooksCount());
        stats.setTotalAvailableCopies(bookDAO.getTotalAvailableCopiesCount());
        stats.setActiveMembers(memberDAO.getActiveMembersCount());
        stats.setBooksIssued(transactionDAO.getBooksIssuedCount());
        stats.setPendingReturns(transactionDAO.getPendingReturnsCount());
        stats.setOverdueBooks(transactionDAO.getOverdueBooksCount());

        stats.setTotalFinesCollected(finePaymentDAO.getTotalFinesCollected());
        double fineRate = settingDAO.getDoubleSetting("fine_per_day", 5.00);
        stats.setOutstandingFines(finePaymentDAO.getTotalOutstandingFines(fineRate));
        return stats;
    }

    public List<ActivityLog> getRecentActivities(int limit) {
        try {
            return activityLogDAO.getRecentLogs(limit);
        } catch (Exception e) {
            System.err.println("Could not load recent activities: " + e.getMessage());
            return List.of();
        }
    }

    public Map<String, int[]> getWeeklyChartData() throws SQLException {
        return transactionDAO.getWeeklyIssueReturnCounts();
    }

    public List<LibraryTransaction> getAllTransactions() throws SQLException {
        return transactionDAO.getAllTransactions();
    }

    public List<LibraryTransaction> getActiveLoans() throws SQLException {
        return transactionDAO.getActiveLoans();
    }

    public double calculateFineForTransaction(LibraryTransaction t, Date calculationDate) {
        if (t == null || t.getDueDate() == null) return 0.0;
        double fineRate = settingDAO.getDoubleSetting("fine_per_day", 5.00);
        int days = DateUtil.calculateOverdueDays(t.getDueDate(), calculationDate);
        return days * fineRate;
    }
}
