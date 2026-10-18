package com.library.model;

import java.sql.Timestamp;

public class Book {
    private int bookId;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private int categoryId;
    private String categoryName; // Joined from categories table
    private String description;
    private int totalCopies;
    private int availableCopies;
    private Integer publicationYear;
    private String coverImage;
    private Timestamp createdAt;

    public Book() {}

    public Book(int bookId, String title, String author, String publisher, String isbn,
                int categoryId, String categoryName, String description,
                int totalCopies, int availableCopies, Integer publicationYear) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.publicationYear = publicationYear;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getIssuedCopies() {
        return Math.max(0, totalCopies - availableCopies);
    }

    public String getStatus() {
        if (availableCopies > 0) {
            return "Available";
        } else {
            return "Issued";
        }
    }

    @Override
    public String toString() {
        return title + " (" + author + ") - " + availableCopies + " avail";
    }
}
