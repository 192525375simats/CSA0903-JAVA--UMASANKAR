# Q3 – The hashCode–equals contract (event attendance de-duplication)

Files: `before/Attendance.java` (TODO 1 NOT done), `after/Attendance.java` (TODO 1 done), `ContractTests.java` (3 designed tests + alternatives), `PREDICTIONS.md`, `output_*.txt/.svg`.
Run: `cd before && javac -d out Attendance.java && java -cp out Attendance` (same in `after`), and `javac -d out ContractTests.java && java -cp out ContractTests`.

## The given program
| Run | Output | Expected |
|---|---|---|
| TODO 1 not completed | `Unique students: 3` | 2 |
| TODO 1 completed (`return regNo.hashCode();`) | `Unique students: 2` | 2 ✔ |
Why the difference: `equals()` already says the two "192311001" scans are equal, but without `hashCode()` the class inherits the identity-based `Object.hashCode()`; two distinct objects have different hashes, so `HashSet` never even calls `equals()` for them.

## Three designed tests (predicted before execution, see `PREDICTIONS.md`; all 12 prediction checks printed MATCH)
1. **Appears to work** – the *same object* scanned twice → size 2 (correct by accident: same reference ⇒ same identity hash and `this == o`). Shows why a quick test can mislead.
2. **Duplicates exposed** – three *different* objects with the same regNo: broken class → size **3**; `equals()` is true but hash codes differ; fixed class → size **1**.
3. **My own tests**
   - (a) *Mutating a field used in hashCode after insertion*: the entry stays in the bucket of the old hash. After `regNo` changes: `contains(sameObject)` = false, `contains(newObjectWithOldRegNo)` = false, `remove` = false, size still 1 → the element is **lost but still occupying memory** (a leak).
   - (b) *TreeSet with a comparator inconsistent with equals* (compare by name): two different students named "Arun" → HashSet size 2 but TreeSet size **1**, because TreeSet treats `compare()==0` as "same element".

## How HashSet uses hashCode() and equals()
`add(x)`: h = x.hashCode() (spread) → bucket index = (n−1) & h → walk the bucket (list or tree): an entry matches only if its **stored hash == h AND (same reference OR x.equals(entry))**. So the contract is: *equal objects must have equal hash codes* (the reverse need not hold). Violating it makes equal objects land in different buckets. If a hashed field changes after insertion, lookups use the new hash and miss the old bucket.

## Correcting and verifying the class
Fix: `@Override public int hashCode() { return regNo.hashCode(); }` – derived from exactly the fields used by `equals()` (only `regNo`), which are `final`/immutable so the hash can never change while in a set (`name` is deliberately not used). Verification: the program output changes 3 → 2; `ContractTests` shows Fixed-class size 1 for three scans; equal objects have equal hashes; a regression check with `HashSet.contains(new Student(sameRegNo, otherName))` is true.

## Alternative structures – cost per scan (measured, ns/scan, 50% repeated scans)
| n unique | HashSet | TreeSet (Comparator on regNo) | HashMap<String,Student> |
|---|---|---|---|
| 10,000 | 116 | 374 | 79 |
| 100,000 | 80 | 671 | 75 |
| 500,000 | 168 | 1,207 | 106 |
HashSet/HashMap: **O(1) average** per scan (hash computation + bucket probe); TreeSet: **O(log n)** string comparisons (671 ns vs 80 ns at 100k, 7–8x). TreeSet needs no hashCode but needs a comparator **consistent with equals** (test 3b) and keeps students sorted. HashMap keyed by regNo avoids relying on `Student.equals/hashCode` at all (String's contract is already correct) and lets you keep the first or latest record. Space for n unique students: all three are O(n); HashSet is backed by a HashMap (node ≈ 32 B + table slot), TreeSet uses a red-black tree entry (≈ 40 B with parent/left/right/colour), HashMap node ≈ 32 B plus the value. (Per-entry sizes in typical HotSpot terms; see Q4 for measured bytes/entry of maps.)
