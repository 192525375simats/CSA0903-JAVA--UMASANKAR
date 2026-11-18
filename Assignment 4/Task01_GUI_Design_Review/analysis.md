# Task 1 – GUI Design Review

## 1.1 Deficiencies and user impact
1. **UI and business logic are mixed:** database and payment rules are inside the frame listener, so changes are difficult to test independently.
2. **String comparison uses ==:** Java compares object references, not string contents; Pay may not be recognised. Use equals or a dedicated listener.
3. **No input validation:** empty register numbers, non-numeric amounts, zero and negative values can cause errors or invalid records.
4. **Blocking database call on the event-dispatch thread:** the window freezes while the database responds.
5. **Thread.sleep(3000):** deliberately freezes repainting and input for three seconds; it is not a bank-confirmation mechanism.
6. **No useful feedback:** printing Paid to the console does not tell the student whether the transaction succeeded or failed.
7. **Receipt button has no separate action:** the else branch clears the form for both Clear and Receipt.
8. **SQL concatenation:** user input can cause SQL injection and quoting errors; use PreparedStatement.
9. **Resource/window lifecycle missing:** the connection and statement are not closed safely and the window has no close handler.
10. **Checked exceptions are not handled:** SQLException and InterruptedException can prevent compilation or crash the flow.

## 1.2 Repaired structure
```text
FeeWindow (collects input, displays feedback)
  └── FeeService (validates and performs payment operation)
        └── PaymentRepository / payment gateway (production integration)
```
The Java implementation uses separate Pay, Clear and Receipt actions. Validation occurs in FeeService and the simulated long-running action is called through SwingWorker, keeping the event-dispatch thread responsive. A production JDBC repository must use PreparedStatement and try-with-resources.

## 1.3 Design defence (under 100 words)
The frame now handles presentation only, while FeeService owns validation and payment rules. FeeService can be unit-tested without opening a window, and the UI can be redesigned without changing payment logic. Separate button actions make each behaviour explicit. SwingWorker prevents slow work from blocking the event-dispatch thread. The design is easier to extend with database transactions, receipt generation and payment status handling.

## Alternative listener concept and complexity
With one action listener and an if/else chain, dispatch may inspect up to k action commands: O(k) worst-case comparisons and O(1) extra dispatch space. With one listener/action per button, the event source directly invokes its registered handler: O(1) dispatch after registration, with O(k) listener objects/references. The latter uses more objects but avoids a growing conditional chain and improves separation.
