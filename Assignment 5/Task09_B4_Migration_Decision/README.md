# B4 – Migration

Run schema.sql in MySQL. From this folder, compile both source files with MySQL Connector/J available. Linux/macOS example:

```bash
javac -cp "../Task08_B3_JDBC_Batch:mysql-connector-j.jar" -d . "../Task08_B3_JDBC_Batch/PassLoader.java" PassMigration.java
java -cp ".:../Task08_B3_JDBC_Batch:mysql-connector-j.jar" PassMigration passes.csv
```

On Windows, use `; ` as the classpath separator rather than `:` (without the extra space). Set DB_URL, DB_USER and DB_PASSWORD. The program prints valid/rejected counts and DatabaseMetaData product, driver and table information. Keep the source CSV format consistent with the five-column assignment format.