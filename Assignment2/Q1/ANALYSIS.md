# Q1 – String vs StringBuilder vs StringBuffer (e-ticket generation)

Files: `TicketBench.java` (code), `PREDICTIONS.md` (written before running), `output.txt` / `output.svg` (real run output, screenshot).
Run: `javac -d out TicketBench.java && java -Xms1g -Xmx3g -cp out TicketBench`

## Workloads
| | Tickets | Fields | Purpose |
|---|---|---|---|
| A | 200,000 | 10 | many short tickets |
| B | 10,000 | 50 | the workload suggested in the question |
| C (my own) | 1 | 20,000 | one very large consolidated booking manifest – exposes quadratic growth |

Measures: total time (average of 5 runs after 2 warm-ups), allocated bytes per ticket (`ThreadMXBean.getCurrentThreadAllocatedBytes`, exact), estimated intermediate String objects, heap delta without GC (noisy – treat only as indicative; allocated bytes is the reliable memory measure).

## Prediction vs observation
| Workload | Prediction | Observed | Verdict |
|---|---|---|---|
| A (10 fields) | `+` <3x slower than builder | `+` 0.193 us vs SB 0.122 us per ticket = **1.6x** | supported |
| B (50 fields) | `+` 2–6x slower, much more garbage | 3.35 us vs 0.60 us = **5.6x**; 23.9 KB vs 3.3 KB allocated per ticket (**7.2x**) | supported |
| C (20,000 fields) | `+` >50x slower | 578 ms vs 0.28 ms ≈ **2,000x**; **3.9 GB** allocated to build one ~0.3 MB ticket | supported (far beyond the prediction) |
| StringBuffer vs StringBuilder, single thread | StringBuffer 10–30% slower | **no measurable slowdown** (A 24.15 vs 24.38 ms, B 4.56 vs 5.98 ms) | **not supported** – the JIT removes/biases uncontended locks; differences are noise |
| Shared log, unsynchronised StringBuilder | loses appends / may throw | only 1/5 trials correct, one trial threw an exception, shortest length 1,764,490 of 2,080,000 expected (**15% of appends lost**) | supported |
| Shared log, StringBuffer | always correct | 5/5 correct (26.5 ms) | supported |

Extra observations: `String.join` was the fastest (B: 0.29 us/ticket) because it knows the final length and allocates once; `StringJoiner` was between. `String.format` cost 2.03 us for a 5-field ticket – about 3–4x the per-field cost of the builder – so it is for readability, not for hot loops.

## Why (String immutability and object creation)
`s = s + f` allocates a new String and copies every character already in `s`. For n fields of average length L: characters copied = L(1+2+…+n) = L·n(n+1)/2 = **O(n²L)**; n intermediate Strings are created (garbage O(n²L) bytes – measured 3.9 GB in workload C). StringBuilder appends into a buffer with amortised doubling: **O(nL)** time, one String at the end (plus O(log n) buffer regrowths, total extra copying ≤ 2·nL). Extra space: `+` keeps at most two big strings alive at a time (O(nL)) but *allocates* O(n²L); StringBuilder allocates O(nL).

## Alternative concept – `String.join`, `StringJoiner`, `String.format`
- `String.join(sep, array)`: O(n) time, one allocation of the exact size – best when all parts are already in a collection/array.
- `StringJoiner`: O(n), builder inside; good when separators/prefix/suffix are needed.
- `String.format`: parses the format string every call; O(n) but with a large constant (measured 2.03 us for 5 fields). Best for a few fields and readability.
- Repeated `+`: O(n²L) – only acceptable for a handful of fields (javac turns one *expression* `a + b + c` into a single efficient concat; the problem is concatenating **in a loop**).

## Design decision (multithreaded booking server)
- **Per-ticket text: `StringBuilder`** (or `String.join` if parts are already in an array). A ticket is built and finished inside one thread, so nothing is shared; synchronisation would only add cost.
- **Shared booking log: not a bare StringBuilder** (measured: 15% of appends lost, crashes). `StringBuffer` is correct (5/5) and simplest; a `StringBuilder` guarded by one `synchronized` block was equally correct (35 ms vs 26 ms here). Better engineering: let each thread build its ticket locally and append the *finished* ticket to the log once (one short critical section per ticket, or a `ConcurrentLinkedQueue`/logging framework), because the lock is then held for microseconds, not for every field.
