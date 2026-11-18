# Task 6 – Campus Event Registration

## Stage 1 – Requirements
Users: students and committee staff. Screens: registration form and committee registration table. Data: student ID, name, event and timestamp. Rules: required fields, valid ID, unique student/event pair, visible errors, responsive loading and CSV export.

Testable statements: blank fields are rejected; valid data is inserted; duplicate student/event pairs are rejected by a database UNIQUE constraint; page query returns at most 50 rows; JDBC runs off the event-dispatch thread; displayed rows can be exported to CSV.

## Stage 2 – Screen and event flow
```text
JFrame
├── JMenuBar: File -> Reload / Exit
├── JPanel: student ID, name, event, Register
├── JTable inside JScrollPane
└── Status JLabel
Register -> validate -> SwingWorker -> Service -> JDBC -> status -> reload
Reload -> SwingWorker -> paginated query -> JTable
Export -> JFileChooser -> CSV
```

## Stage 3 – Critical decisions
UI validation gives immediate feedback; service validation enforces business rules. Duplicate prevention is enforced by UNIQUE(student_id,event_name), which avoids race conditions in a separate pre-check. PreparedStatement prevents SQL injection. SwingWorker.doInBackground performs database work; done updates Swing components on the EDT.

## Stage 4 – Class design and pseudocode
```text
EventRegistrationApp (JFrame / presentation)
       -> RegistrationService (use cases / validation)
       -> JDBC repository (SQL / data access)
       -> MySQL registrations table
```
Register: read fields; validate required values and ID; disable submit; insert in SwingWorker; unique key rejects duplicate; show result; refresh first page; re-enable controls.
Filter: read selected event; query with WHERE event_name=? ORDER BY registered_at DESC LIMIT ? OFFSET ? in SwingWorker; replace JTable rows in done().

## Stage 5 – Implementation
EventRegistrationApp.java uses JFrame, JPanel, JTextField, JComboBox, JTable, JMenuBar, SwingWorker and JDBC. schema.sql creates the table. Configure MySQL and Connector/J, then demonstrate normal, invalid and duplicate registrations.

## Alternative: all 100,000 rows vs pagination
Loading all N=100,000 rows transfers O(N) records and uses O(N) client memory; if performed on the EDT it freezes the UI. Page size P transfers O(P) records and uses O(P) table memory per page; total traversal still costs O(N). SwingWorker keeps either query off the EDT. Deep OFFSET queries can become slower; indexed keyset pagination can improve this.
