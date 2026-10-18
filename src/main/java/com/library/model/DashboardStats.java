package com.library.model;

public class DashboardStats {
    private int totalBooks;
    private int totalAvailableCopies;
    private int activeMembers;
    private int booksIssued;
    private int pendingReturns;
    private int overdueBooks;
    private double totalFinesCollected;
    private double outstandingFines;

    public DashboardStats() {}

    public int getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(int totalBooks) {
        this.totalBooks = totalBooks;
    }

    public int getTotalAvailableCopies() {
        return totalAvailableCopies;
    }

    public void setTotalAvailableCopies(int totalAvailableCopies) {
        this.totalAvailableCopies = totalAvailableCopies;
    }

    public int getActiveMembers() {
        return activeMembers;
    }

    public void setActiveMembers(int activeMembers) {
        this.activeMembers = activeMembers;
    }

    public int getBooksIssued() {
        return booksIssued;
    }

    public void setBooksIssued(int booksIssued) {
        this.booksIssued = booksIssued;
    }

    public int getPendingReturns() {
        return pendingReturns;
    }

    public void setPendingReturns(int pendingReturns) {
        this.pendingReturns = pendingReturns;
    }

    public int getOverdueBooks() {
        return overdueBooks;
    }

    public void setOverdueBooks(int overdueBooks) {
        this.overdueBooks = overdueBooks;
    }

    public double getTotalFinesCollected() {
        return totalFinesCollected;
    }

    public void setTotalFinesCollected(double totalFinesCollected) {
        this.totalFinesCollected = totalFinesCollected;
    }

    public double getOutstandingFines() {
        return outstandingFines;
    }

    public void setOutstandingFines(double outstandingFines) {
        this.outstandingFines = outstandingFines;
    }
}
