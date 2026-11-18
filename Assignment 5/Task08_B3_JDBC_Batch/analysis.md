# B3 – Statement vs PreparedStatement vs Batch

PassLoader.java completes all three TODOs: bind five parameters, add rows to a batch and execute each 1,000 rows; execute the final partial batch; commit once; rollback and rethrow on SQL or runtime failure. The previous auto-commit setting is restored.

Run PassLoaderBenchmark with MySQL to record 1,000, 10,000 and 100,000 row timings. It compares Statement (one execution per row), PreparedStatement (one execution per row) and PreparedStatement batch in one transaction. Use the same server, table, network and JVM for fair measurements. Batching reduces round trips; a transaction reduces commit overhead and makes the batch atomic.

Run BatchRollbackTest to force an invalid amount halfway through the list. Parsing throws before commit; rollback leaves zero inserted rows. Verify with SELECT COUNT(*).

For 100,000 nightly imports, use PreparedStatement batching with a transaction (or LOAD DATA for very large trusted imports after validation). For a single counter issue, PreparedStatement is simpler and safely parameterised. Statement is not recommended for user data. Multi-row INSERT can reduce round trips but query size grows with batch; LOAD DATA can be fastest for bulk ingestion but needs strict staging/validation and server privileges. Approximate client batch memory is O(b × rowSize); streaming fixed-size batches bounds memory, whereas collecting all n rows is O(n).
