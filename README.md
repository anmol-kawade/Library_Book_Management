# 📚 College Library Book Lending System

A comprehensive, production-grade Core Java console application for managing college library book lending, memberships, transactions, and overdue charge calculations.

Developed as a 4-person collaborative project following OOP best practices, Java Collections, Lambda Expressions, Custom Exception Handling, and File Persistence.

---

## 🌟 Features

- **📖 Book Management**:
  - Add, edit, remove, and list books in the catalog.
  - Search books by title, author, or category using Lambda Expressions (`Predicate<Book>`).
  - Filter books by availability status.
- **👥 Member Management**:
  - Register Student and Faculty members with polymorphic borrowing rules:
    - **Students**: Max 3 books, 14-day loan duration, ₹5.00/day overdue fine.
    - **Faculty**: Max 6 books, 30-day loan duration, ₹2.00/day overdue fine.
  - View member details and complete borrowing histories.
- **🔄 Lending Operations**:
  - Issue books with availability checks, member quota limits, and automatic due date calculation.
  - Return books with automatic overdue day tracking and fine calculation.
  - View active loans with real-time overdue alerts.
- **📊 Reports & Analytics**:
  - Overdue books report with estimated outstanding fines.
  - Full transaction history sorted chronologically using Stream comparators.
  - Members holding active loans.
  - Library executive summary snapshot.
- **💾 File Persistence**:
  - Automatically loads and saves data across sessions to standard CSV files in `data/`.
  - Manual backup and reset to default demo dataset.
- **🛡️ Bulletproof Exception Handling & Validation**:
  - Custom domain exceptions: `BookNotAvailableException`, `BookNotFoundException`, `MemberNotFoundException`, `InvalidReturnException`, `MaxBooksLimitExceededException`.
  - Safe console input validator preventing number format crashes and EOF errors.

---

## 📁 Project Structure

```
.
├── src/
│   └── com/
│       └── library/
│           ├── Main.java                        # Application entry point & Console UI
│           ├── model/
│           │   ├── Person.java                  # Abstract base class
│           │   ├── Member.java                  # Abstract member class (polymorphic)
│           │   ├── StudentMember.java           # Student subclass (quota: 3, period: 14d, fine: ₹5/d)
│           │   ├── FacultyMember.java           # Faculty subclass (quota: 6, period: 30d, fine: ₹2/d)
│           │   ├── Book.java                    # Book entity
│           │   └── IssueRecord.java             # Transaction record
│           ├── service/
│           │   ├── BookService.java             # Book CRUD, search & lambda filtering
│           │   ├── MemberService.java           # Member directory & validation
│           │   ├── LendingService.java          # Issue, return, quota check & fine calculation
│           │   └── ReportService.java           # Analytics & reports via Java Streams
│           ├── storage/
│           │   └── FileStorageService.java      # CSV file persistence (java.io)
│           ├── exception/
│           │   ├── LibraryException.java        # Base checked exception
│           │   ├── BookNotFoundException.java
│           │   ├── BookNotAvailableException.java
│           │   ├── MemberNotFoundException.java
│           │   ├── InvalidReturnException.java
│           │   └── MaxBooksLimitExceededException.java
│           ├── util/
│           │   ├── DateUtils.java               # Date formatting & fine calculation
│           │   └── InputValidator.java          # Robust console input reading
│           └── test/
│               └── LibraryTestRunner.java       # Automated test suite (35 tests)
├── data/                                        # Persistent CSV files
│   ├── books.csv
│   ├── members.csv
│   └── issues.csv
├── docs/
│   ├── PROJECT_REPORT.md                        # Formal project report & team division
│   └── TEST_CASES.md                            # Test specifications, edge cases & outputs
└── README.md
```

---

## 🚀 How to Compile & Run

### Prerequisites
- **Java JDK 8 or higher** (JDK 17, 21, 27 supported)
- Standard command terminal (PowerShell, Bash, or CMD)

### 1. Compile the Project
Open a terminal in the project root directory and run:

**PowerShell (Windows):**
```powershell
javac -d bin -sourcepath src (Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName })
```

**Bash / Linux / macOS / Git Bash:**
```bash
javac -d bin -sourcepath src $(find src -name "*.java")
```

---

### 2. Run the Main Application
```bash
java -cp bin com.library.Main
```

---

### 3. Run the Automated Test Suite (35 Unit & Integration Tests)
```bash
java -cp bin com.library.test.LibraryTestRunner
```

---

## 👥 Team Work Division

- **Student 1**: Requirement Analysis, System Architecture Design, Console Interface Menu Navigation.
- **Student 2**: Class Design, OOP Planning (`Person` -> `Member` -> `StudentMember`/`FacultyMember` hierarchy), Core Entities Implementation.
- **Student 3**: Core Business Features (Book/Member Management, Issue/Return), Input Validation, Custom Exceptions.
- **Student 4**: CSV File Persistence (`FileStorageService`), Lambda Expressions & Streams, Analytical Reports (`ReportService`).
- **All Students**: Verification of edge cases, data reload testing, test documentation, and live demonstration rehearsal.

---

## 📄 Documentation Links
- Full Project Report: [PROJECT_REPORT.md](docs/PROJECT_REPORT.md)
- Complete Test Cases & Edge Cases: [TEST_CASES.md](docs/TEST_CASES.md)
