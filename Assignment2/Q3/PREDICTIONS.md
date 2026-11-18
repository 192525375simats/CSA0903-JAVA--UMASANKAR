# Q3 – Predictions (written BEFORE running)

| # | Test | Predicted HashSet size | Reason |
|---|---|---|---|
| 0 | Given program, TODO 1 NOT completed (hashCode missing) | **3** (expected 2) | `equals` says the two "192311001" scans are equal, but `Object.hashCode()` is identity-based, so the two objects almost surely land in different buckets / carry different hashes; HashMap compares hash first and never calls equals. |
| 0b | Same program with `hashCode() = regNo.hashCode()` | **2** | equal objects now have equal hashes, same bucket, equals() confirms duplicate. |
| 1 | Scans that "appear to work": the SAME Student object is scanned twice (same reference) | **2** (looks correct) | identical reference => identical identity hash => same bucket, `this == o` is true. Works by accident. |
| 2 | Deliberate duplicates: 3 different objects with the same regNo, broken class | **3** (fixed class: 1) | different identity hashes. |
| 3a | Mutable key: add student, then change regNo (fixed hashCode, non-final field) | size stays 1, but `contains(student)` = **false** and `remove` fails | entry stored in bucket of the OLD hash; lookup uses the NEW hash. The entry is lost (memory leak). |
| 3b | TreeSet with comparator by name while equals uses regNo; two different students named "Arun" | TreeSet size **1**, HashSet size **2** | TreeSet uses compare()==0 as equality; HashSet uses hashCode/equals. |
