# Q4 – Choosing a Map for a pharmacy inventory

Files: `PharmacyBench.java`, `PREDICTIONS.md` (before running), `output.txt/.svg`.
Run: `javac -d out PharmacyBench.java && java -Xms1g -Xmx3g -cp out PharmacyBench`

## Design (before implementation – see PREDICTIONS.md)
Expected costs: HashMap/LinkedHashMap put & get O(1) average, iterate O(n); TreeMap put & get O(log n), iterate O(n) in key order. Natural reports: **code-order → TreeMap**, **insertion-order → LinkedHashMap**. "Wrong map" extra work: HashMap/LinkedHashMap need an O(n log n) sort for the code-order report; HashMap/TreeMap need an extra list (O(n) memory, maintained on every insert) for the insertion-order report.

## Test: n = 1,000 / 10,000 / 100,000 / 500,000 medicines (codes `MED00000001`…, inserted in shuffled order so insertion order ≠ code order); best of 3 trials.
| n | map | put all (ms) | 100k lookups (ms) | ns/lookup | code-order report (ms) | insertion report (ms) |
|---|---|---|---|---|---|---|
| 100,000 | HashMap | 15.9 | 3.9 | 39 | 34.1 (sort) | 0.33 (side list) |
| 100,000 | TreeMap | 57.1 | 54.5 | 545 | **8.2** (native) | 0.35 (side list) |
| 100,000 | LinkedHashMap | 12.3 | 4.6 | 46 | 38.4 (sort) | **1.26** (native) |
| 100,000 | Hashtable | 10.0 | 6.0 | 60 | 22.6 (sort) | 0.37 (side list) |
| 500,000 | HashMap | 100 | 10.2 | 102 | 193 (sort) | 4.8 |
| 500,000 | TreeMap | 642 | 118 | 1,184 | **48.7** | 5.2 |
| 500,000 | LinkedHashMap | 79 | 9.5 | 95 | 313 (sort) | **9.7** |
(full table for all sizes is in `output.txt`.) Output order check: the program compares each report with the independently sorted/ordered expected list – **all reports correct**; raw iteration order is natural for code only in TreeMap and for insertion only in LinkedHashMap, as designed.
Observed scaling: TreeMap lookup 199 → 272 → 545 → 1,184 ns for ×1…×500 size (log n plus cache misses); HashMap 23 → 62 → 39 → 102 ns (≈ flat; noise at small n).

## Scalability inquiry
**Acceptable response time** (my criterion): ≤ 10 µs average per lookup (so 100,000 lookups < 1 s) – far below the ~100 ms a human notices, leaving the budget for I/O. All maps pass it even at 500,000 medicines (worst: TreeMap 1.2 µs). With only "hundreds of lookups a day", even TreeMap would be adequate; the decision therefore rests on the *reports* and on headroom.
**Deploy: `LinkedHashMap`** for the inventory – lookup cost equal to HashMap (46 vs 39 ns at 100k), insertion-order report free, and the code-order report is one sort per evening (38 ms at 100,000 medicines, 313 ms at 500,000 – irrelevant for a nightly job). Use `TreeMap` instead if code-ordered range queries ("all codes starting MED12") or ordered reports are frequent: it is 8x cheaper for that report but 12x slower per lookup and 6–8x slower to build at 500k.
**Where the remaining cost comes from:** (i) memory latency – each lookup touches the table, a node, the key String and its bytes (cache misses dominate at 500k); (ii) String.hashCode/equals over the 11-char key (the hash is cached after the first call); (iii) for TreeMap ~log₂ n ≈ 19 String comparisons per lookup.
**Why Hashtable is not preferred in new code:** legacy class with every method `synchronized` (one coarse lock) – measured slower than HashMap even single-threaded (60 vs 39 ns at 100k, 121 vs 102 ns at 500k), forbids null keys/values, gives no ordering benefit. If thread safety is needed use `ConcurrentHashMap` (lock-free reads, fine-grained writes) or wrap with `Collections.synchronizedMap` knowingly.

## Alternative: sorted array + `Arrays.binarySearch`
- Lookup O(log n): 524 ns at 100k – same as TreeMap, 13x slower than HashMap.
- Insertion O(n) per item (shift): building 100,000 by repeated insertion took **455 ms vs 16 ms** for HashMap (O(n²) total); bulk-load once then sort is O(n log n) – good only for a read-mostly catalogue.
- Code-order report: free, O(n). Insertion-order report: needs a side list.
- Space: smallest – ≈ 25 bytes/entry (value object only; keys shared) vs measured **HashMap ≈ 71, TreeMap ≈ 64, LinkedHashMap ≈ 84 bytes/entry** (200,000 entries, after GC, includes the shared key/value objects counted once).
Comparison: HashMap = fastest lookup, no order; LinkedHashMap = HashMap + insertion order (+13 B/entry); TreeMap = sorted, O(log n); sorted array = compact + sorted but static.
