# A5 – Design for Correctness and Safety

## A5.1 Atomic wallet transfer pseudocode
START; GET JDBC connection; SET autoCommit=false; VALIDATE amount > 0; UPDATE hostel_wallet SET balance=balance-500 WHERE wallet_id=? AND balance>=500; IF affected rows != 1 THEN ROLLBACK and REPORT insufficient funds; UPDATE canteen_wallet SET balance=balance+500 WHERE wallet_id=?; IF affected rows != 1 THEN ROLLBACK and REPORT failure; COMMIT; on any SQLException ROLLBACK and rethrow; CLOSE connection; STOP. Use a single database transaction and suitable isolation/row locking. Both updates succeed or neither does.

## A5.2 Injection
Concatenating untrusted values into SQL lets an attacker enter SQL syntax such as a quote followed by an OR condition and comment marker, changing the WHERE predicate. For example an ID of `' OR '1'='1' -- ` can turn the predicate into a condition that is always true (exact behaviour depends on SQL dialect and quoting). A PreparedStatement uses placeholders and binds ID/password as data, so quotes are not interpreted as SQL syntax. Store salted password hashes and verify with a password-hashing library; do not compare plaintext passwords in production.

## Alternative: auto-commit updates
With auto-commit, the debit may commit before a crash prevents the credit, losing money; the reverse order can create money. A transaction has a small coordination/logging overhead but protects the invariant. Both approaches execute two updates (O(1) statements at this abstraction level; actual cost depends on indexes, locks and disk log), while transaction correctness is decisively stronger.
