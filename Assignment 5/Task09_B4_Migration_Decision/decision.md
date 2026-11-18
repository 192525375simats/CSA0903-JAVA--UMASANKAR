# B4 – Final Engineering Decision

## B4.1 Migration strategy
PassMigration reads the existing delimited file line-by-line, validates field count, required text, route and amount, and accumulates valid records while recording rejected line numbers/reasons. It passes valid rows to PassLoader, which inserts in 1,000-row batches inside one transaction. For very large files, process bounded chunks and write rejected rows to a durable report rather than retaining all valid rows in memory.

## B4.2 Database metadata
The program reports DatabaseMetaData.getDatabaseProductName(), getDatabaseProductVersion(), getDriverName(), getDriverVersion() and table names. This reveals server/driver differences during deployment and helps diagnose missing tables or incompatible JDBC drivers.

## B4.3 Production decision
For 12 campuses, 100× volume and concurrent counters, use a central relational database with primary/unique keys, indexes, transactions, access control, backups and connection pooling. A text file is useful for interchange/import but does not safely coordinate concurrent writers or indexed queries. Java serialization is Java-specific, brittle across schema evolution and unsuitable as the shared multi-user system of record. The database supports concurrent transactions and indexed pass-ID/route searches.

## Alternative: 12 independent serialized files
Each campus can search its local file in O(n_i), but a global search must inspect all campus files, O(N) total where N is total passes, and manage 12 copies/versions. Storage is O(N) plus duplicated metadata. Concurrent local writers need file locks or per-campus queues; cross-campus consistency, conflict resolution and global reporting are difficult. A central database handles concurrency and consistency through transactions and indexes; a campus cache may be used for resilience but should not become a conflicting source of truth.
