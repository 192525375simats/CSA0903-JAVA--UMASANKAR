# Q4 – Predictions (written BEFORE running)

| Map | put | get | iterate | natural report |
|---|---|---|---|---|
| HashMap | O(1) avg | O(1) avg | O(n + capacity), arbitrary order | neither (needs extra sort for code order; needs a separate list for insertion order) |
| TreeMap | O(log n) | O(log n) | O(n), sorted by code | **code order** directly |
| LinkedHashMap | O(1) avg | O(1) avg | O(n), insertion order | **insertion order** directly |
| Hashtable | O(1) avg but synchronised | O(1) synchronised | arbitrary | legacy: coarse locking, no benefit |

Expected measurements: 100,000 lookups: HashMap ~ LinkedHashMap (a few ms), TreeMap several times slower (log n string comparisons, cache misses), growing with n. Code-order report: TreeMap cheapest; HashMap/LinkedHashMap need O(n log n) sort. Insertion-order report: LinkedHashMap cheapest; others cannot produce it without extra structure.
Acceptable response time (my criterion): one lookup <= 10 microseconds average (i.e. 100,000 lookups < 1 s), so a pharmacist never perceives delay even if the system also does I/O.
Expected decision: HashMap/LinkedHashMap for lookups; LinkedHashMap if the insertion report is mandatory; code-order report produced by one sort per evening (cheap) OR TreeMap if code-order range queries are needed. Sorted array + binarySearch: lookup O(log n) but insertion O(n).
