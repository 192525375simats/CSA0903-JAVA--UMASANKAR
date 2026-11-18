# B2 – Engineer an Adversarial Input

The unsafe Statement concatenates untrusted values into SQL. The ID input `' OR '1'='1' -- ` can close the quoted ID, add an always-true OR predicate and comment out the remaining password condition (MySQL comment syntax requires whitespace after --). PreparedStatement binds the same characters as a literal parameter, so the input is not parsed as SQL syntax.

The Java program creates a disposable local demo_users table, inserts one test account, then compares unsafe and safe results. Expected: unsafe=true, prepared=false, valid safe login=true.

Malformed pass file cases: wrong number of columns, non-numeric amount and empty line. PassStore skips these invalid records and continues. Escaping quotes or regex validation is weaker than parameter binding because it is easy to miss SQL syntax/encoding edge cases; PreparedStatement separates code from data. Validation is O(length of input) per query, but it does not eliminate injection risk by itself.
