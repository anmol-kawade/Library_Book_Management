# Project Report: College Library Book Lending System

**Course**: Core Java Programming / Library Management Systems  
**Domain**: Object-Oriented Software Engineering & File Processing  
**Team Size**: 4 Members  

---

## 1. Executive Summary & Objective

The **College Library Book Lending System** is a robust console-based application built to automate and streamline the day-to-day operations of a college library. The system manages cataloged books, student and faculty memberships, issue and return workflows, overdue fine calculations, and persistent file storage. 

Built using **Java SE (JDK 8 or later)**, the application showcases core object-oriented programming principles, the Java Collections Framework, lambda expressions with functional interfaces, custom exception hierarchies, and CSV file persistence.

---

## 2. Team Member Work Division & Contributions

As mandated by the project guidelines, work was systematically partitioned across four team members:

| Team Member | Module Assigned | Key Contributions & Deliverables |
| :--- | :--- | :--- |
| **Student 1** | **Module 1 & 7**: Requirement Analysis, System Architecture & Console Integration | - Analyzed library workflows and identified entity attributes.<br>- Formulated functional specifications and console menu designs.<br>- Structured application packages and coordinated integration between modules. |
| **Student 2** | **Module 2 & 3**: Class Design, OOP Planning & Core Domain Classes | - Designed `Book`, `Person`, `Member`, `StudentMember`, and `FacultyMember` entities.<br>- Implemented inheritance hierarchy and runtime polymorphism for borrowing rules.<br>- Created `IssueRecord` and implemented constructors, getters, setters, and `toString()` methods. |
| **Student 3** | **Module 4 & 5**: Core Business Logic, Input Validation & Custom Exceptions | - Implemented book catalog CRUD and member registration services.<br>- Implemented lending business logic (issue, return, availability toggling).<br>- Developed `InputValidator` and created domain exception hierarchy (`BookNotAvailableException`, `InvalidReturnException`, `MaxBooksLimitExceededException`, etc.). |
| **Student 4** | **Module 6 & 7**: File Storage, Lambda Processing, Reporting & Integration | - Built `FileStorageService` for reading and writing CSV files.<br>- Developed `ReportService` leveraging Java Streams and Lambda expressions.<br>- Implemented fine calculation, overdue reporting, and automated serialization across repeated runs. |
| **All Members** | **Module 8**: Testing, Documentation & Live Demonstration | - Formulated unit and integration test suite (`LibraryTestRunner`).<br>- Verified edge cases and persisted data reloading.<br>- Authored test documentation and rehearsed the final presentation. |

---

## 3. System Architecture & Class Design

### 3.1 Domain Model Hierarchy
```
                   +------------------+
                   |     Person       | (Abstract Base Class)
                   +------------------+
                   | - id: String     |
                   | - name: String   |
                   | - contact: String|
                   | - email: String  |
                   +--------+---------+
                            |
                            | (Inheritance)
                   +--------v---------+
                   |     Member       | (Abstract Class)
                   +------------------+
                   | - regDate        |
                   | - borrowedCount  |
                   +--------+---------+
                            |
            +---------------+---------------+
            |                               | (Polymorphism)
+-----------v-----------+       +-----------v-----------+
|     StudentMember     |       |     FacultyMember     |
+-----------------------+       +-----------------------+
| maxBooks: 3           |       | maxBooks: 6           |
| loanDays: 14          |       | loanDays: 30          |
| dailyFine: ₹5.00/day  |       | dailyFine: ₹2.00/day  |
+-----------------------+       +-----------------------+
```

### 3.2 Key Entities and Responsibilities

1. **`Book`**: Encapsulates `id`, `title`, `author`, `category`, and boolean `available`. Provides getters/setters, formatted display, and CSV serialization.
2. **`Person` & `Member`**: An abstract inheritance chain modeling library users.
   - `StudentMember`: Limit 3 books, loan duration 14 days, fine ₹5.00 per day.
   - `FacultyMember`: Limit 6 books, loan duration 30 days, fine ₹2.00 per day.
3. **`IssueRecord`**: Encapsulates lending transactions including `recordId`, `bookId`, `bookTitle`, `memberId`, `memberName`, `issueDate`, `dueDate`, `returnDate`, `fineAmount`, and `status`.
4. **`BookService`**: Encapsulates a `HashMap<String, Book>` for constant-time lookups. Implements search and filtering via lambda predicates.
5. **`MemberService`**: Manages the member directory in a `HashMap<String, Member>`.
6. **`LendingService`**: Enforces business rules: prevents issuing checked-out books, checks member borrowing quotas, computes due dates, and tracks active loans.
7. **`ReportService`**: Provides analytic queries (overdue books, total fines collected, loan histories) via Java Streams.
8. **`FileStorageService`**: Persists records to `data/books.csv`, `data/members.csv`, and `data/issues.csv` using UTF-8 text I/O.
9. **`InputValidator`**: Prevents runtime crashes from invalid user keystrokes (number formats, regex phone/email checks, date formats).

---

## 4. Key Core Java Concepts Applied

### 4.1 Object-Oriented Programming (OOP)
- **Encapsulation**: All class fields are declared `private` and accessed controlled through getters, setters, and business methods.
- **Inheritance**: `StudentMember` and `FacultyMember` extend `Member`, which extends `Person`. Common personal data and behaviors are reused cleanly.
- **Polymorphism**: The abstract methods `getMaxBooksAllowed()`, `getLoanPeriodDays()`, and `getDailyFineRate()` are overridden in concrete subclasses. When a member borrows or returns a book, the lending engine dynamically evaluates loan durations and fine rates without needing `instanceof` cascades or hardcoded `if-else` blocks.
- **Abstraction**: High-level services interact with `Member` abstractions rather than tight couplings to specific member implementations.

### 4.2 Collections Framework
- **`HashMap<String, Book>` & `HashMap<String, Member>`**: Used for $O(1)$ key-based indexing by Book ID and Member ID.
- **`ArrayList<IssueRecord>`**: Used to store chronological transaction logs and support filtering and sorting.
- **Collections Sorting**: Utilizing `Collections.sort()` and `Comparator.comparing(...)`.

### 4.3 Custom Exception Handling
A specialized hierarchy rooted at `LibraryException` prevents crashes and communicates domain rule violations:
- `BookNotFoundException`: Search/lookup misses.
- `MemberNotFoundException`: Non-existent member IDs.
- `BookNotAvailableException`: Attempting to borrow an already checked-out book.
- `MaxBooksLimitExceededException`: Attempting to exceed member loan limits.
- `InvalidReturnException`: Returning an unissued book or returning with invalid dates.

All console input operations are wrapped in safe `try-catch` structures so invalid entries display informative messages without program termination.

### 4.4 Functional Programming & Lambda Expressions
- **`Predicate<Book>`**: Enables reusable book filtering by title, author, or availability.
  ```java
  public List<Book> searchByTitle(String query) {
      String lower = query.toLowerCase();
      return filterBooks(book -> book.getTitle().toLowerCase().contains(lower));
  }
  ```
- **Stream Reductions (`mapToDouble`, `sum`)**: Computes cumulative overdue fines:
  ```java
  double totalFines = records.stream()
      .filter(r -> IssueRecord.STATUS_RETURNED.equals(r.getStatus()))
      .mapToDouble(IssueRecord::getFineAmount)
      .sum();
  ```
- **Stream Sorting**: Reverse-chronological transaction ordering:
  ```java
  issues.stream()
      .sorted(Comparator.comparing(IssueRecord::getIssueDate).reversed())
      .collect(Collectors.toList());
  ```

### 4.5 Java File I/O & Persistence
- Uses `BufferedReader`, `PrintWriter`, and `FileInputStream`/`FileOutputStream` with UTF-8 encoding.
- Supports CSV formatting with quote handling for strings containing commas.
- Automatically creates storage files on first launch and syncs updates immediately.

---

## 5. Live Demonstration Rehearsal Plan

For the live team demonstration, each team member demonstrates their respective module:

1. **Student 1 (Architectural Overview & Book Management)**:
   - Starts application: demonstrates startup data loading.
   - Adds a new book and lists the catalog.
   - Demonstrates lambda-based search by title and author.
2. **Student 2 (Class Hierarchy & Member Registration)**:
   - Registers a `StudentMember` (quota = 3, loan period = 14 days).
   - Registers a `FacultyMember` (quota = 6, loan period = 30 days).
   - Explains the `Person` -> `Member` inheritance and polymorphism.
3. **Student 3 (Lending Operations & Exception Handling)**:
   - Issues a book to a student; demonstrates automatic due date computation.
   - **Edge Case 1**: Tries to issue the same book again -> demonstrates `BookNotAvailableException`.
   - **Edge Case 2**: Attempts to issue books exceeding borrowing quota -> demonstrates `MaxBooksLimitExceededException`.
   - **Edge Case 3**: Returns an unissued book -> demonstrates `InvalidReturnException`.
4. **Student 4 (Reports, Overdue Calculation & File Persistence)**:
   - Runs Overdue Report: shows overdue days calculation and dynamic daily rate calculation.
   - Returns an overdue book: verifies calculated fine imposition.
   - Exits application, verifies persisted data in `data/*.csv`, re-launches app, and demonstrates preserved state.
