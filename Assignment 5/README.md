# Assignment 5 – Data Persistence Engineering (CO5)

**Student:** Umasankar  
**Register number:** 192525375  
**Course:** Programming in Java  
**Java version:** OpenJDK 17+  
**Database:** MySQL 8.x with MySQL Connector/J 8.x  
**Repository:** https://github.com/192525375simats/CSA0903-JAVA--UMASANKAR

This folder follows the uploaded ASSIGNMENT5.pdf. It has one folder for each question: A1–A5 and B1–B4. Part A contains the required record designs, pseudocode and engineering analysis; Part B contains Java source, SQL, test inputs, output records and visual evidence placeholders.

## Run instructions
1. Install JDK 17 or newer and verify with `java -version` and `javac -version`.
2. Compile standalone programs from their task folder: `javac FileName.java`, then `java ClassName`.
3. JDBC tasks require MySQL 8.x, run the task's `schema.sql`, add MySQL Connector/J to the classpath, and set `DB_URL`, `DB_USER`, and `DB_PASSWORD` environment variables. Never commit real credentials.
4. Output text files document expected/test scenarios. SVG files are labelled UI/terminal preview illustrations, not photographs of a live execution. Run the programs in your own environment and replace them with genuine screenshots before submission.
5. Benchmarks are environment-dependent. Run the supplied benchmark programs on the same machine and record actual measurements; do not present sample figures as measured results.

## Task index
| Folder | Question | Main deliverable |
|---|---|---|
| Task01_A1_File_Format | A1 | Text/binary layouts, operations, recovery, complexity |
| Task02_A2_Buffered_IO | A2 | Read-call estimate, buffered counter, memory analysis |
| Task03_A3_DAO_Pattern | A3 | Generic DAO, Student mapping and JDBC implementation |
| Task04_A4_Serialization | A4 | Serializable object graph, transient fields, versioning |
| Task05_A5_Transactions_Injection | A5 | Atomic wallet transfer and safe login |
| Task06_B1_File_Store | B1 | PassStore TODO, damaged-line tests, I/O benchmarks |
| Task07_B2_Adversarial_Login | B2 | Unsafe vs PreparedStatement login and malformed inputs |
| Task08_B3_JDBC_Batch | B3 | Statement/prepared/batch loader, transaction tests |
| Task09_B4_Migration_Decision | B4 | File migration, failure report, metadata and decision |

**Submission reminder:** Capture genuine execution screenshots for each task, add them alongside the source and output, and update the README with your actual Java/database versions and measured timings.
