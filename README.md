# Library Management System (LMS)

A complete, production-grade **Library Management System** built with **Java (JDK 17+)**, **direct JDBC**, **MySQL 8.0+**, and a modern **Java Swing GUI** inspired by the provided orange-and-white dashboard reference design.

![UI Theme](https://img.shields.io/badge/Theme-Orange%20%26%20White-FF6B00?style=for-the-badge)
![Tech Stack](https://img.shields.io/badge/Stack-Java%2017%20%7C%20JDBC%20%7C%20MySQL%20%7C%20Swing-blue?style=for-the-badge)

---

## 1. Features & Screens

### 🎨 Clean Orange & White UI
- **Exact visual reference match**: Soft warm off-white canvas (`#F8FAFC`), pure white cards with subtle borders (`#E5E7EB`), vibrant orange primary accent (`#FF6B00`), and dark charcoal readability (`#1E293B`).
- **Resolution-independent vector icons**: Books, users, clocks, alerts, search, bell, settings, edit, trash, and charts rendered with anti-aliasing via Java 2D Graphics.

### 🔐 1. Login Screen
- Split-screen branding banner with book illustration and tagline.
- Admin authentication with secure **BCrypt** password verification.
- Remember-me toggle, clear error notifications, and instant "Sign in as Admin" quick-fill.

### 📊 2. Dashboard
- Live database metrics calculated directly from MySQL:
  - **Total Books**
  - **Active Members**
  - **Books Issued**
  - **Pending Returns & Overdue**
- **Dual Grouped Bar Chart** showing Issues vs. Returns for the last 7 days (Mon–Sun).
- **Recent Activities Feed** with relative timestamps (*"2 mins ago"*, *"1 hour ago"*).
- **Quick Actions Bar** for one-click access to add books, register members, or issue loans.

### 📚 3. Books Management
- Add, edit, and delete books with strict referential integrity rules.
- Filter tabs: `All Books`, `Available`, `Issued`, `Lost/Damaged`.
- Filter by category, sort by title/author/copies, and search in real time.
- Validates ISBN uniqueness and non-negative copy quantities.
- Multi-page pagination bar (`Previous`, `1`, `2`, `3`, ..., `Next`).

### 👥 4. Members Management
- Register new members with automatic Member ID generation (`M1001`, `M1002`, ...).
- Filter by membership type (`Student`, `Faculty`, `Staff`) or status (`Active`, `Inactive`).
- Enforces unique email constraints and validates phone numbers.
- Deactivate members or safely delete inactive members without active loans.

### 🔄 5. Issue & Return Books
- **Atomic ACID Transactions**: Uses `setAutoCommit(false)`, `commit()`, and `rollback()` so book loan records and available copy counts succeed or fail together.
- **Issue Mode**:
  - Live member details sidebar showing active loans and eligibility.
  - Automatic due date calculation based on configurable loan periods (e.g., 14 days).
  - Enforces borrowing limits and blocks unverified/inactive accounts.
- **Return Mode**:
  - Search active loans by book or member.
  - Automatic overdue calculation and fine assessment based on system fine rate.
  - Option to settle fines upon return with multiple payment methods (Cash, Card, UPI, Online).

### 🏷️ 6. Categories Management
- Organize books into subjects/categories.
- Live book counter per category.
- Safeguard: Prevents deletion of categories currently associated with books.

### 💰 7. Fines & Payment Management
- Displays total collected fines vs. outstanding overdue balances.
- Filter between pending fines and historical payment receipts.
- Settle outstanding balances partially or in full with timestamped receipts.

### 📈 8. Reports & Analytics
- Pre-built reporting queries:
  - Book Inventory Status
  - Currently Borrowed Books
  - Overdue Borrowers & Fines
  - Most Frequently Borrowed Books
  - Fine Collection History
- **Export to CSV**: One-click export button to save any report to a clean UTF-8 CSV file.

### ⚙️ 9. Settings & Database Connectivity
- Configure library display name, default loan duration (days), fine rate per day, and max borrowing limit.
- Secure admin password change.
- Live MySQL connection tester and dynamic JDBC reconfiguration without application restart.

---

## 2. Technology Stack & Architecture

- **Language:** Java 17 or higher
- **GUI Framework:** Java Swing with FlatLaf modern look & feel
- **Connectivity:** Direct **JDBC** (`java.sql.*`), `PreparedStatement`, `ResultSet`
- **Driver:** MySQL Connector/J (`com.mysql:mysql-connector-j:8.3.0`)
- **Security:** BCrypt (`org.mindrot:jbcrypt:0.4`)
- **Build Tool:** Apache Maven

### Layered Architecture
```
com.library
├── Main.java               # Application entry point & L&F initialisation
├── config/                 # JDBC connection manager & DB configuration
├── model/                  # Domain POJOs (Book, Member, Transaction, etc.)
├── dao/                    # Pure JDBC Data Access Objects
├── service/                # Business logic, validations, & multi-step operations
├── ui/                     # Swing frames and main application window
│   ├── components/         # Reusable rounded cards, buttons, tables, charts
│   ├── panels/             # Screen panels (Dashboard, Books, Members, etc.)
│   └── theme/              # Color constants, vector icon painter, fonts
└── util/                   # BCrypt password utility, validators, CSV exporter
```

---

## 3. Database Setup (MySQL)

### Step 1: Create Database and Tables
Open your MySQL terminal or MySQL Workbench and run:
```bash
mysql -u root -p < database/schema.sql
```

### Step 2: Insert Sample Data
Populate the database with sample categories, books, members, transactions, and default admin:
```bash
mysql -u root -p < database/sample_data.sql
```

### Step 3: Configure Connection Settings
Edit `src/main/resources/db.properties` or set environment variables:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=your_mysql_password
```

*(Note: If MySQL is not running when you first launch the app, a local MySQL-compatible embedded engine will seamlessly activate so the application can be previewed and tested immediately!)*

---

## 4. Default Admin Credentials

| Field | Value |
|---|---|
| **Username** | `admin` |
| **Password** | `admin123` |

*(Password can be updated anytime under **Settings** → **Change Admin Password**)*

---

## 5. How to Build & Run

### Prerequisites
- JDK 17 or higher installed (`java -version`)
- Maven installed (`mvn -version`)

### Build Executable JAR
```bash
mvn clean package
```

### Run Application
```bash
# Run compiled classes directly:
mvn exec:java -Dexec.mainClass="com.library.Main"

# Or run the Fat JAR generated in target/:
java -jar target/library-management-system-1.0.0.jar
```

---

## 6. Verification & Test Scenarios

1. **Book Validation:** Try adding a book without title/author or with negative copies — proper validation dialogs appear.
2. **Duplicate ISBN Prevention:** Attempting to save an existing ISBN is caught and rejected before database write.
3. **Email Uniqueness:** Member registration prevents duplicate email addresses.
4. **Loan Enforcement:** Inactive members or members who have reached the borrowing limit cannot be issued books.
5. **Availability Decrement/Increment:** Issuing decreases available copies by 1; returning restores available copies.
6. **ACID Transaction Rollback:** Returning an already returned transaction or issuing an unavailable book is safely aborted with automatic rollback.
7. **CSV Export:** Select any report in Reports & Analytics and click **Export to CSV** to verify output.
