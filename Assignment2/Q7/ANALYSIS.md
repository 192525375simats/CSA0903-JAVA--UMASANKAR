# Q7 – Day-4 Design Challenge: diagnose, redesign, validate

**Solution selected: Q1 – building the e-ticket with `s = s + field` in a loop.** Files: `TicketRedesign.java`, `PREDICTION.md` (Part D, written before implementing), `output.txt/.svg`.
Run: `javac -d out TicketRedesign.java && java -Xms1g -Xmx3g -cp out TicketRedesign`

## A. Diagnose the bottleneck (evidence from Q1 and Q7 baseline)
- **Where time and memory go:** copying. Workload C (20,000 fields): the original copies **≈ 3.0 × 10⁹ characters** to produce a 306,707-char ticket (≈ 10,000x the ticket length) and allocates **2.9–3.9 GB**; 578 ms (Q1) / 438 ms (Q7) versus 0.28–0.46 ms with a builder.
- **Which operation grows:** the cost of one ticket grows **quadratically** with the number of fields: 5,000 fields 35 ms → 10,000 fields 119 ms (×3.4) → 20,000 fields 438 ms (×3.7); doubling the fields ≈ ×4. (Workload B, 50 fields: only 18.9 KB allocated and ≈ 16,265 chars copied per 649-char ticket, i.e. 25x the ticket size.)
- **Unnecessary repeated work:** every `+` re-creates the whole String and re-copies the entire prefix (n−1 copies of data already built); the plain `StringBuilder` baseline still creates a new builder and re-grows its buffer (8→16→…) for **every** ticket.
- **Characteristic of the class that causes it:** `String` immutability – a "modification" is a new object plus a full copy. (For the plain builder: its buffer is private to one call and discarded.)

## B. Design objective (measurable)
Produce byte-identical tickets while: (1) creating exactly **one String per ticket**; (2) copying each character **O(1)** times (≈ ≤ 2× the ticket length, not O(n²)); (3) allocating **≤ 10 % of the original's bytes** for 50-field tickets; (4) being ≥ **2x faster** than the original on workload B; (5) no per-ticket builder creation or regrowth; (6) preserving correctness (outputs compared for equality) and thread-safety by confinement.

## C. Revised solution
```
class TicketFormatter                       // one instance per thread (thread-confined; no locks)
  labels[i]  <- names[i] + "="              // built once per ticket TYPE
  buf        <- StringBuilder(256)          // reused
  hint       <- 256                         // largest ticket length seen
  format(pnr, vals):
      buf.setLength(0); buf.ensureCapacity(hint)      // reuse, no regrowth
      buf.append("PNR:").append(pnr).append('\n')
      for i in 0..n-1:  buf.append(labels[i]).append(vals[i]).append("; ")   // ints appended directly, no temp Strings
      buf.append("\n-- end --")
      hint <- max(hint, buf.length())
      return buf.toString()                            // the only object created per ticket
```
- **What changed:** `+` loop → one reusable, pre-sized builder; labels precomputed; ints appended without intermediate Strings; one formatter per thread (verified with 4 threads: checksums identical to the single-thread result).
- **Property exploited:** `StringBuilder` is mutable with amortised O(1) appends and `setLength(0)` keeps its capacity – the buffer survives across tickets.
- **Operations avoided/reduced:** O(n²) prefix copying, n intermediate Strings, per-ticket builder allocation and buffer doubling.
- **Additional memory/preprocessing:** the formatter retains a buffer as large as the largest ticket per thread (≈ 0.3–0.6 MB for 306,707 chars) and an O(n) labels array; one-off setup per ticket type.
- **New trade-offs:** stateful, **not thread-safe** (must stay thread-confined, e.g. `ThreadLocal`/one per worker); memory is retained instead of being freed after each ticket; more code than a one-liner.

## D. Prediction (made before implementing – `PREDICTION.md`)
Big win from removing `+` (workload C ≫ 50x, B ≈ 3–6x, D ≈ 1.5–3x); redesign vs plain StringBuilder a further ≈ 1.2–2x on B, ≈ 1.3–2x on D, ≈ 1x on C.

## E. Measured results – identical conditions (average of 5 runs after 2 warm-ups; outputs verified identical)
| Workload | version | total ms | µs/ticket | alloc KB/ticket | chars copied/ticket |
|---|---|---|---|---|---|
| B 10,000×50 | original (+) | 29.50 | 2.95 | 18.92 | 16,265 (exact) |
| | plain StringBuilder | 13.07 | 1.31 | 3.08 | ≤ ~1,947 (est.) |
| | **redesign** | **9.07** | **0.91** | **0.72** | ~1,298 (est.) |
| C 1×20,000 | original | 438.4 | 438,412 | 2,937,235 | 3,006,551,595 (exact) |
| | plain StringBuilder | 0.46 | 457 | 1,452 | ≤ ~920,121 |
| | **redesign** | **0.36** | **365** | **300** | ~613,414 |
| D 100,000×10 | original | 26.96 | 0.270 | 1.45 | 710 |
| | plain StringBuilder | 20.52 | 0.205 | 0.55 | ≤ ~405 |
| | **redesign** | **17.19** | **0.172** | **0.20** | ~270 |
| E 1×5,000 | original / SB / redesign | 35.4 / 0.12 / 0.09 | | 177,179 / 216 / 72 | |
| F 1×10,000 | original / SB / redesign | 119.3 / 0.24 / 0.17 | | 717,391 / 721 / 144 | |
Speed-ups: redesign vs original = **3.3x (B), 1,202x (C), 1.6x (D)**; redesign vs plain StringBuilder = **1.44x (B), 1.25x (C), 1.19x (D)**. Allocation on B: 0.72 vs 18.92 KB (**3.8 %** of the original). Concurrency check passed. (Time figures for C/E/F are fractions of a millisecond and carry timer/JIT noise; allocation and copy counts are the dependable evidence there.)

## F. Defence of the final design
- **Was the prediction supported?** Largely, with corrections. *Supported:* huge gain from removing `+` (C: 1,202x), redesign beats plain builder by 1.44x on B (predicted 1.2–2x), allocation falls to ≈ 1 String per ticket. *Not fully supported:* the builder-vs-original ratio on B was **2.3x**, below my 3–6x (Q1's pure-concatenation test showed 5.6x; here `Integer`→text conversion costs the same in all versions and dilutes the ratio); on D the redesign's gain over the builder was **1.19x**, a little under 1.3–2x; on C I predicted "≈ the same" but got 1.25x because the reused buffer is already large from earlier calls (this reuse across calls is exactly what the design exploits and exists in a server, but in a single cold call it would not help).
- **Magnitude of improvement:** from the original, **1.6x on short tickets up to ~1,200x on huge ones**; the larger the ticket, the larger the gain, because the original is O(n²) and the redesign O(n). The redesign's *extra* gain over the plain builder is modest (≈ 1.2–1.45x, 4x–7x less allocation on B/C).
- **New costs:** retained buffer memory per thread, statefulness/thread-confinement rule, extra code to maintain.
- **When the original is still preferable:** tickets with a handful of fields (D: 0.27 vs 0.17 µs – a 0.1 µs difference nobody can notice), code that is not on a hot path, and when readability/immutability matter (javac compiles a single `a + b + c` expression into one efficient concatenation; the pathology is concatenation **in a loop**). The plain `StringBuilder` per ticket is the best balance for most services; the reusable formatter pays off in a high-volume booking server where allocation rate and GC matter.
- **Conclusion:** the redesign met its objective (identical output, 1 String per ticket, 3.8 % of the allocation on B, 3.3x faster on B), the dominant improvement comes from eliminating the quadratic `+` pattern, and the incremental benefit of reuse is real but small.
