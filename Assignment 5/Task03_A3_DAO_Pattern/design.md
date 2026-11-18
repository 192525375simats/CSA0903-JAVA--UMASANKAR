# A3 – Discover the Data-Access Design Pattern

## A3.1 Repeated pattern
Repeated steps: obtain connection, prepare SQL, bind parameters, execute, iterate ResultSet, map columns to objects, handle exceptions and close resources. Differences: table/column names, entity fields, key type and SQL for each entity.

## A3.2 Generic DAO
```java
public interface Dao<T,K> {
  K insert(T entity) throws java.sql.SQLException;
  java.util.Optional<T> findById(K id) throws java.sql.SQLException;
  java.util.List<T> findAll() throws java.sql.SQLException;
  boolean update(T entity) throws java.sql.SQLException;
  boolean delete(K id) throws java.sql.SQLException;
}
```
StudentDao implements Dao<Student,Integer>. It owns SQL and maps ResultSet rows to Student. Services call the interface rather than embedding SQL in GUI code.

## A3.3 Object-table mapping
Student(id, registerNo, name, email) maps to student(id PRIMARY KEY, register_no UNIQUE NOT NULL, name NOT NULL, email). id is the primary key; Java fields map through ResultSet getters and PreparedStatement setters. See StudentDao.java and schema.sql.

## Alternative: SQL in service/GUI
A renamed column must be updated in every service/GUI location containing SQL, increasing coupling and missed-change risk. With DAO, SQL mapping is centralised in one repository implementation. Both designs have essentially the same database operation complexity (for example indexed lookup O(log n) or engine-dependent index cost) and similar row-transfer memory; DAO adds a small constant abstraction overhead, not an asymptotic cost.
