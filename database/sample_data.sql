-- ==========================================================
-- Library Management System Sample Data
-- Database: library_db
-- ==========================================================

USE library_db;

-- Default Settings
INSERT INTO settings (setting_key, setting_value, description) VALUES
('library_name', 'Library System', 'Display name of the library'),
('loan_period_days', '14', 'Default loan period in days'),
('fine_per_day', '5.00', 'Fine amount charged per day for overdue returns'),
('max_books_per_member', '5', 'Maximum number of books an individual can borrow simultaneously'),
('currency_symbol', '₹', 'Currency symbol for fines and transactions');

-- Default Admin User (username: admin, password: admin123)
-- BCrypt hash for 'admin123': $2a$10$FCR6Zm617Jge8FjPpA0ahOKi6kZZkQH7AjvckzQKd7idrgplVJGRG
INSERT INTO admin (username, password_hash, full_name, email) VALUES
('admin', '$2a$10$FCR6Zm617Jge8FjPpA0ahOKi6kZZkQH7AjvckzQKd7idrgplVJGRG', 'System Administrator', 'admin@library.com');

-- Sample Categories
INSERT INTO categories (category_id, category_name, description) VALUES
(1, 'Computer Science', 'Algorithms, systems, databases, and core computing concepts'),
(2, 'Programming', 'Languages, frameworks, design patterns, and software development'),
(3, 'Mathematics', 'Engineering mathematics, calculus, linear algebra, and discrete math'),
(4, 'Literature', 'Classic and contemporary literary fiction, poetry, and prose'),
(5, 'Science & Physics', 'Theoretical physics, mechanics, optics, and general science');

-- Sample Books (Matching UI Reference Template)
INSERT INTO books (book_id, title, author, publisher, isbn, category_id, description, total_copies, available_copies, publication_year) VALUES
(1, 'Data Structures and Algorithms', 'Mark Allen Weiss', 'Pearson Education', '978-0132576277', 1, 'In-depth coverage of fundamental algorithms, trees, graphs, and algorithmic complexity.', 8, 5, 2014),
(2, 'Operating System Concepts', 'Abraham Silberschatz', 'Wiley', '978-1119800361', 1, 'The definitive guide to processes, memory management, file systems, and virtualization.', 5, 2, 2021),
(3, 'Database System Concepts', 'Abraham Silberschatz & Korth', 'McGraw-Hill', '978-0078022159', 1, 'Comprehensive reference on relational modeling, SQL, transaction processing, and concurrency control.', 6, 4, 2019),
(4, 'Engineering Mathematics', 'B.S. Grewal', 'Khanna Publishers', '978-8174091955', 3, 'Higher engineering mathematics covering differential equations, vector calculus, and numerical methods.', 10, 6, 2020),
(5, 'Introduction to Java', 'Herbert Schildt', 'McGraw-Hill Education', '978-1260440232', 2, 'Complete beginner to advanced guide to Java syntax, OOP, collections, and modern Java features.', 4, 1, 2022),
(6, 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', 'Prentice Hall', '978-0132350884', 2, 'Principles, patterns, and practices of writing maintainable, readable software.', 5, 5, 2008),
(7, 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', 'Addison-Wesley', '978-0201633610', 2, 'The seminal Gang of Four catalog of object-oriented design patterns.', 4, 3, 1994);

-- Sample Members (Matching UI Reference Template)
INSERT INTO members (member_id, member_code, name, email, phone, membership_type, registration_date, status) VALUES
(1, 'M1001', 'Arun V', 'arun@email.com', '9876543210', 'Student', '2026-01-15', 'Active'),
(2, 'M1002', 'Priya S.', 'priya@email.com', '8765432109', 'Student', '2026-02-10', 'Active'),
(3, 'M1003', 'Rohit Kumar', 'rohit@college.edu', '7654321098', 'Faculty', '2026-01-05', 'Active'),
(4, 'M1004', 'Sarah Khan', 'sarah@library.org', '9123456780', 'Staff', '2026-03-01', 'Active'),
(5, 'M1005', 'Vikram Patel', 'vikram@email.com', '9811223344', 'Student', '2026-02-20', 'Inactive');

-- Sample Transactions (Issued & Returned Loans matching screenshot activities)
INSERT INTO transactions (trans_id, book_id, member_id, issue_date, due_date, return_date, status, notes) VALUES
(1, 1, 3, '2026-10-18', '2026-11-01', NULL, 'ISSUED', 'Issued to Faculty for course preparation'),
(2, 2, 2, '2026-10-01', '2026-10-15', '2026-10-18', 'RETURNED', 'Returned 3 days overdue, fine settled'),
(3, 5, 1, '2026-10-10', '2026-10-24', NULL, 'ISSUED', 'Standard student issue'),
(4, 2, 1, '2026-09-20', '2026-10-04', '2026-10-04', 'RETURNED', 'Returned on time in good condition'),
(5, 7, 3, '2026-10-14', '2026-10-28', NULL, 'ISSUED', 'Reference copy checkout');

-- Sample Fine Payments
INSERT INTO fine_payments (payment_id, trans_id, member_id, amount, payment_date, payment_method, notes) VALUES
(1, 2, 2, 50.00, '2026-10-18', 'Cash', 'Fine settlement for 3 days overdue on Operating System Concepts');

-- Sample Activity Logs (Matching UI Recent Activities)
INSERT INTO activity_logs (log_id, admin_username, action_type, description, created_at) VALUES
(1, 'admin', 'ISSUE_BOOK', 'Rohit Kumar issued a book: Data Structures and Algorithms', '2026-10-18 12:44:00'),
(2, 'admin', 'RETURN_BOOK', 'Priya S. returned a book: Operating System Concepts', '2026-10-18 12:31:00'),
(3, 'admin', 'NEW_MEMBER', 'New member registered: Arun V (M1001)', '2026-10-18 11:46:00'),
(4, 'admin', 'FINE_PAYMENT', 'Fine payment received: ₹50.00 from Priya S. (M1002)', '2026-10-18 10:46:00'),
(5, 'admin', 'ADD_BOOK', 'Added new book: Clean Code to library inventory', '2026-10-17 15:20:00');
