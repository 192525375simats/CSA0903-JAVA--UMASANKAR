# Assignment 4 – Event-Driven GUI Design (CO4)

**Student:** Umasankar  
**Register number:** 192525375  
**Course:** Programming in Java (CSA09)  
**Java:** OpenJDK 21 (Swing/AWT); Task 6 requires MySQL Connector/J 8.x  
**Repository:** https://github.com/192525375simats/CSA0903-JAVA--UMASANKAR

This folder follows the six tasks in the SIMATS Assignment 4 PDF. Each task has its own folder with source code and example output evidence. GUI output files are labelled mock-up/test evidence where a real interactive desktop screenshot is not available in this GitHub upload.

## Compile and run
Open a terminal in the relevant task folder. For a single-file Swing/AWT task:
```bash
javac FileName.java
java ClassName
```
On a desktop with a graphical display, run the GUI program. A headless server cannot display Swing/AWT windows.

For Task 6, add MySQL Connector/J to the classpath and configure the database credentials using environment variables `DB_URL`, `DB_USER`, and `DB_PASSWORD`. Run `schema.sql` first.

| Task | Folder | Main file | Result |
|---|---|---|---|
| 1 | Task01_GUI_Design_Review | FeeWindow.java | Separates UI and FeeService, validates data, uses prepared SQL pattern and background worker |
| 2 | Task02_Appointment_Desk | AppointmentDesk.java | Department-driven doctor list, booking validation and close confirmation |
| 3 | Task03_Library_Layout | LibrarySearch.java | Nested BorderLayout/FlowLayout layout; centre results area expands |
| 4 | Task04_Event_Handling_Forensics | DrawPad.java | Correct mouseClicked handler, persistent points and repaint |
| 5 | Task05_Event_Handling_Styles | ConverterLambda.java plus three style variants | Four listener styles and Enter-key/dialog requirement |
| 6 | Task06_Campus_Event_Registration | EventRegistrationApp.java, schema.sql | Layered Swing/JDBC sample, duplicate constraint and paginated background loading |

## Important execution notes
- Task 1's payment service is a demonstration stub: connect it to a real payment gateway/database only with approved credentials and a secure backend.
- Task 2 uses an in-memory doctor catalogue to demonstrate event flow.
- Task 6 is the JDBC implementation and requires a local MySQL database plus Connector/J. Never commit real passwords.
- The output.txt files are expected/sample console or test-case records, not claims of screenshots from an actual execution on your computer. The SVG files are labelled visual previews. Run each program locally and replace/add genuine screenshots of your own execution before submission.
- Task 3 resize screenshots should be captured at three sizes from a desktop session; suggested sizes are 600x400, 850x550 and 1100x700.
