# A4 – What Should Be Serialized?

## A4.1 Fields and object graph
Hostel, Room and Resident must implement Serializable if they are part of the reachable object graph. Serialization recursively traverses referenced serializable objects. Mark the live JDBC Connection transient because it is an external resource, not persistent state, and it is not safely serializable. Mark password transient (or preferably do not store it in this graph at all); credentials should be stored as salted password hashes in a secure credential store. Other transient fields may include caches, open streams and runtime UI references. A transient field is restored with its default value (null for references) and must be reinitialised after deserialization if needed.

## A4.2 Schema evolution
Declare an explicit serialVersionUID in every serializable class. If a new phoneNumber field is added while keeping the same serialVersionUID, an older stream can generally be read and the new field receives its default value (null); use readObject/readResolve or migration logic to set a safe default. If serialVersionUID is incompatible, InvalidClassException is thrown. Changing class structure still requires deliberate compatibility tests.

## Alternative: CSV/JSON
Text formats are usually more verbose than Java binary serialization but are language-independent and inspectable. Both save/load n residents in O(n) time and use O(n) memory if the entire graph is materialised (streaming parsers can reduce memory). CSV requires escaping and explicit schema rules; JSON supports named fields and can tolerate missing optional fields more naturally. Java serialization is convenient for Java object graphs but is tightly coupled to class definitions and can be unsafe with untrusted input. Never deserialize untrusted bytes without a strict filter.
