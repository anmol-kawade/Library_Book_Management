# Test Cases and Verification Document
**Project**: College Library Book Lending System  
**Course**: Core Java Programming / Library Management Systems  

---

## 1. Automated Test Suite Summary

An automated test suite (`com.library.test.LibraryTestRunner`) tests all functional requirements, OOP polymorphism, custom exceptions, lambda processing, and file persistence.

```
================================================================
       RUNNING AUTOMATED TEST SUITE FOR LIBRARY SYSTEM          
================================================================

Test Group 1: Book CRUD Operations
  [PASS] Book added and exists
  [PASS] Retrieved book title matches
  [PASS] Book title updated
  [PASS] Book removed successfully

Test Group 2: Member Polymorphism & Registration
  [PASS] Student max books allowed is 3
  [PASS] Student loan period is 14 days
  [PASS] Student daily fine rate is ₹5.0
  [PASS] Faculty max books allowed is 6
  [PASS] Faculty loan period is 30 days
  [PASS] Faculty daily fine rate is ₹2.0
  [PASS] Members registered in service

Test Group 3: Normal Issue & Return Lending Flow
  [PASS] Book availability updated to false after issue
  [PASS] Member borrowed count incremented
  [PASS] Issue record status is ISSUED
  [PASS] Due date correctly set to issueDate + 14 days
  [PASS] Book availability updated to true after return
  [PASS] Member borrowed count decremented to 0
  [PASS] Issue record status is RETURNED
  [PASS] No fine on timely return

Test Group 4: Edge Case - Issuing Unavailable Book
  [PASS] Caught BookNotAvailableException as expected: Book 'System Design' (ID: B401) is currently checked out.

Test Group 5: Edge Case - Borrowing Quota Limit Exceeded
  [PASS] Issued 3 books successfully
  [PASS] Caught MaxBooksLimitExceededException as expected: Member 'Limit Tester' (ID: S501) has reached the maximum borrowing limit of 3 book(s).

Test Group 6: Edge Case - Returning Book That Was Never Issued
  [PASS] Caught InvalidReturnException as expected: Book 'Unissued Book' (ID: B601) is already in library inventory and was not issued.

Test Group 7: Overdue Fine Calculation (Student ₹5/day, Faculty ₹2/day)
  [PASS] Student overdue days is 5
  [PASS] Student fine is ₹25.0
  [PASS] Faculty overdue days is 10
  [PASS] Faculty fine is ₹20.0

Test Group 8: Lambda Expressions, Filtering & Reporting
  [PASS] Lambda filtered 2 Java books
  [PASS] Lambda filtered 2 available books
  [PASS] Books list non-empty

Test Group 9: File Persistence and Cross-run Reload Verification
  [PASS] Reloaded books contain BP1
  [PASS] Reloaded book BP2 availability preserved as false
  [PASS] Reloaded member SP1 is instance of StudentMember
  [PASS] Reloaded member FP1 is instance of FacultyMember
  [PASS] Reloaded issues contains REC-P1

================================================================
TEST RESULTS: 35 PASSED, 0 FAILED
================================================================
```

---

## 2. Detailed Manual Test Cases & Sample I/O

### Test Case 1: Issue an Available Book
* **Description**: Verify that a registered member can borrow an available book.
* **Input**:
  - Main Menu -> Option `3` (Lending Operations)
  - Submenu -> Option `1` (Issue Book)
  - Book ID: `B101`
  - Member ID: `S103`
  - Issue Date: `[Enter for Today]`
* **Expected Result**: Book marked unavailable (`false`), member active loans incremented, due date set to today + 14 days (for student), issue record created.
* **Sample Output**:
  ```text
  [+] BOOK ISSUED SUCCESSFULLY!
  Transaction Record Details:
    Record ID : REC-1003
    Book      : [B101] Clean Code
    Member    : [S103] Rohan Verma
    Issue Date: 2026-11-02
    Due Date  : 2026-11-16
  ```

---

### Test Case 2: Edge Case - Issuing Unavailable Book
* **Description**: Attempting to issue a book that is currently checked out.
* **Input**:
  - Book ID: `B103` (Currently borrowed by Aarav Sharma)
  - Member ID: `S103`
* **Expected Result**: System intercepts the operation without crashing, throwing `BookNotAvailableException`.
* **Sample Output**:
  ```text
  [-] Availability Error: Book 'Effective Java' (ID: B103) is currently checked out.
  ```

---

### Test Case 3: Edge Case - Maximum Quota Limit Exceeded
* **Description**: Attempting to borrow a 4th book for a Student member whose limit is 3.
* **Input**:
  - Member with 3 active loans (`S501`) requests book `B104`.
* **Expected Result**: System catches `MaxBooksLimitExceededException` and cancels transaction.
* **Sample Output**:
  ```text
  [-] Quota Error: Member 'Limit Tester' (ID: S501) has reached the maximum borrowing limit of 3 book(s).
  ```

---

### Test Case 4: Edge Case - Returning an Unissued Book
* **Description**: Attempting to return a book that is already available on the shelf.
* **Input**:
  - Return Book ID: `B102` (Status: Available)
* **Expected Result**: `InvalidReturnException` thrown and handled gracefully.
* **Sample Output**:
  ```text
  [-] Return Error: Book 'Introduction to Algorithms' (ID: B102) is already in library inventory and was not issued.
  ```

---

### Test Case 5: Overdue Return and Fine Calculation
* **Description**: Returning a book 6 days past its due date for a student member (daily rate ₹5.00).
* **Input**:
  - Return Book ID: `B105` (Due Date: 2026-10-27)
  - Return Date: `2026-11-02`
* **Calculation**:
  $$\text{Overdue Days} = 2026\text{-}11\text{-}02 - 2026\text{-}10\text{-}27 = 6\text{ days}$$
  $$\text{Fine} = 6 \times \text{₹}5.00 = \text{₹}30.00$$
* **Sample Output**:
  ```text
  [+] BOOK RETURNED SUCCESSFULLY!
  Transaction Summary:
    Record ID   : REC-1002
    Book        : [B105] The Pragmatic Programmer
    Member      : [S102] Priya Patel
    Issue Date  : 2026-10-13
    Due Date    : 2026-10-27
    Return Date : 2026-11-02
    [!] Overdue Days: 6 day(s)
    [$] Fine Imposed: ₹30.00 (Collected)
  ```

---

### Test Case 6: Input Validation (Dates, Numbers, Empty Strings)
* **Description**: Testing invalid console inputs to ensure the app never crashes with unhandled exceptions.
* **Input**:
  - Choosing menu option: typing `"abc"` or `99`
  - Entering invalid date format: `"tomorrow"`
  - Entering invalid phone: `"1234"`
* **Expected Result**: Friendly warning messages and prompt re-displayed without exiting.
* **Sample Output**:
  ```text
  Enter your choice (1-6): abc
  [!] Invalid input! Please enter a valid number.
  Enter your choice (1-6): 99
  [!] Value must be between 1 and 6.
  Enter Contact Phone: 1234
  [!] Invalid phone number format! (Must be 10-13 digits, optional + prefix).
  Enter Return Date: 02-11-2026
  [!] Invalid date format! Please use YYYY-MM-DD (e.g., 2026-11-02).
  ```

---

### Test Case 7: Data Reload Verification Across Repeated Runs
* **Description**: Close application and re-launch to verify state persistence in `data/*.csv`.
* **Steps**:
  1. Add a new book `B109: "Clean Architecture"`.
  2. Exit application with Option `6` (Save & Exit).
  3. Start application again.
  4. View Book Catalog (Option 1 -> 1).
* **Expected Result**: `B109` is persisted in `data/books.csv` and reloaded into memory automatically.
