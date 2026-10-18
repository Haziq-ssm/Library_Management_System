package com.library.service;

import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class BookService {

    private final BookDAO bookDAO;

    public BookService() {
        this.bookDAO = new BookDAO();
    }

    public List<Book> getAllBooks() throws SQLException {
        return bookDAO.getAllBooks();
    }

    public List<Book> searchAndFilter(String keyword, Integer categoryId, String statusFilter, String sortBy) throws SQLException {
        return bookDAO.searchAndFilter(keyword, categoryId, statusFilter, sortBy);
    }

    public Book getBookById(int bookId) throws SQLException {
        return bookDAO.getBookById(bookId);
    }

    public boolean saveBook(Book book) throws SQLException {
        if (book == null) {
            throw new IllegalArgumentException("Book details are required.");
        }

        if (book.getBookId() > 0) {
            Book currentBook = bookDAO.getBookById(book.getBookId());
            if (currentBook == null) {
                throw new IllegalArgumentException("The book being edited no longer exists.");
            }
            int checkedOutCopies = currentBook.getTotalCopies() - currentBook.getAvailableCopies();
            if (book.getTotalCopies() < checkedOutCopies) {
                throw new IllegalArgumentException(
                        "Total copies cannot be lower than the " + checkedOutCopies + " copies currently on loan.");
            }
            book.setAvailableCopies(book.getTotalCopies() - checkedOutCopies);
        }

        validateBook(book);

        if (book.getBookId() > 0) {
            return bookDAO.updateBook(book);
        } else {
            return bookDAO.addBook(book);
        }
    }

    public boolean deleteBook(int bookId) throws SQLException {
        return bookDAO.deleteBook(bookId);
    }

    private void validateBook(Book book) throws SQLException {
        if (!ValidationUtil.isNotEmpty(book.getTitle())) {
            throw new IllegalArgumentException("Book title is required.");
        }
        if (!ValidationUtil.isNotEmpty(book.getAuthor())) {
            throw new IllegalArgumentException("Book author is required.");
        }
        if (book.getCategoryId() <= 0) {
            throw new IllegalArgumentException("A book category is required.");
        }
        if (book.getTotalCopies() <= 0) {
            throw new IllegalArgumentException("Total copies must be at least 1.");
        }
        if (book.getAvailableCopies() < 0 || book.getAvailableCopies() > book.getTotalCopies()) {
            throw new IllegalArgumentException("Available copies must be between 0 and total copies (" + book.getTotalCopies() + ").");
        }
        if (ValidationUtil.isNotEmpty(book.getIsbn())) {
            if (bookDAO.isIsbnExists(book.getIsbn(), book.getBookId() > 0 ? book.getBookId() : null)) {
                throw new IllegalArgumentException("A book with ISBN '" + book.getIsbn() + "' already exists in the system.");
            }
        }
    }
}
