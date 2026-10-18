package com.library.service;

import com.library.dao.FinePaymentDAO;
import com.library.dao.SettingDAO;
import com.library.dao.TransactionDAO;
import com.library.model.FinePayment;
import com.library.model.LibraryTransaction;
import com.library.util.DateUtil;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FineService {

    private final FinePaymentDAO finePaymentDAO;
    private final TransactionDAO transactionDAO;
    private final SettingDAO settingDAO;

    public FineService() {
        this.finePaymentDAO = new FinePaymentDAO();
        this.transactionDAO = new TransactionDAO();
        this.settingDAO = new SettingDAO();
    }

    public List<LibraryTransaction> getTransactionsWithFines() throws SQLException {
        List<LibraryTransaction> all = transactionDAO.getAllTransactions();
        List<LibraryTransaction> withFines = new ArrayList<>();
        double fineRate = settingDAO.getDoubleSetting("fine_per_day", 5.00);

        for (LibraryTransaction t : all) {
            Date effectiveReturn = t.getReturnDate() != null ? t.getReturnDate() : DateUtil.today();
            int overdueDays = DateUtil.calculateOverdueDays(t.getDueDate(), effectiveReturn);
            if (overdueDays > 0 || t.getFinePaid() > 0) {
                t.setOverdueDays(overdueDays);
                t.setCalculatedFine(overdueDays * fineRate);
                if (t.getFineBalance() > 0.005) {
                    withFines.add(t);
                }
            }
        }
        return withFines;
    }

    public boolean recordPayment(int transId, int memberId, double amount, Date paymentDate,
                                 String paymentMethod, String notes, String adminUsername) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        if (paymentDate == null) {
            paymentDate = DateUtil.today();
        }

        FinePayment fp = new FinePayment();
        fp.setTransId(transId);
        fp.setMemberId(memberId);
        fp.setAmount(amount);
        fp.setPaymentDate(paymentDate);
        fp.setPaymentMethod(paymentMethod != null ? paymentMethod : "Cash");
        fp.setNotes(notes);

        double fineRate = settingDAO.getDoubleSetting("fine_per_day", 5.00);
        return finePaymentDAO.recordPayment(fp, adminUsername, fineRate);
    }

    public List<FinePayment> getAllPaymentHistory() throws SQLException {
        return finePaymentDAO.getAllPayments();
    }

    public double getTotalCollected() throws SQLException {
        return finePaymentDAO.getTotalFinesCollected();
    }

    public double getTotalOutstanding() throws SQLException {
        double fineRate = settingDAO.getDoubleSetting("fine_per_day", 5.00);
        return finePaymentDAO.getTotalOutstandingFines(fineRate);
    }
}
