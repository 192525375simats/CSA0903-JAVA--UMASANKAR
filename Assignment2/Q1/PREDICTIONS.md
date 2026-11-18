# Q1 – Predictions (written BEFORE running the benchmark)

| Workload | String `+` loop | StringBuilder | StringBuffer |
|---|---|---|---|
| A: 1 short ticket x 10 fields (200,000 repeats) | slightly slower; the 10 tiny copies are cheap, difference small (<3x) | fastest or tied | ~ same as StringBuilder (uncontended lock is cheap, maybe 10-30% slower) |
| B: 10,000 tickets x 50 fields | clearly slower (roughly 2-6x) and allocates far more garbage | fastest | close to StringBuilder |
| C (my own): 1 huge ticket x 20,000 fields | dramatically slower (O(n^2) copying, expect >50x) | fastest | close to StringBuilder |

Reason: `String` is immutable, so `s = s + f` allocates a new String and copies all characters so far.
For n fields of average length L, characters copied = L(1+2+...+n) = O(n^2 L), and n intermediate Strings are created.
StringBuilder appends into a growing char/byte buffer: amortised O(1) per char, O(nL) total, one String at the end.
StringBuffer is the same algorithm with every method `synchronized`; uncontended locking costs little.

Multithreaded shared log (8 threads x 20,000 appends):
- StringBuilder (unsynchronised): predicted to LOSE appends / even throw ArrayIndexOutOfBounds (not thread-safe).
- StringBuffer: predicted correct length every time.
