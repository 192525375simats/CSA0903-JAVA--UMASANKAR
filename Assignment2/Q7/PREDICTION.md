# Q7 – Prediction (Part D) written BEFORE implementing/measuring the redesign

Chosen solution: **Q1 e-ticket generation** (the original `s = s + field` loop).
Diagnosis basis: Q1 data (see Q1/output). Redesign objective (Part B): produce byte-identical tickets while (1) creating exactly one String per ticket, (2) copying each character O(1) times (no O(n^2) work), (3) allocating <= 1/10 of the original bytes for 50-field tickets, and (4) removing per-ticket StringBuilder creation / growth.

Predicted behaviour (testable):
| Workload | Original `+` | Plain StringBuilder (Q1 answer) | Redesign (reused, pre-sized, thread-confined builder + precomputed labels) |
|---|---|---|---|
| B: 10,000 x 50 fields | 1.0x (baseline) | ~3-6x faster, ~10x less allocation | a further ~1.2-2x faster than plain SB, allocation ~ = 1 String per ticket |
| C: 1 x 20,000 fields | 1.0x | >50x faster (O(n) vs O(n^2)) | roughly the same as plain SB (single huge ticket: reuse cannot help, growth cost is one-off) |
| D: 100,000 x 10 fields | 1.0x | ~1.5-3x faster | the largest relative gain over plain SB (~1.3-2x) because per-ticket builder creation matters most for short tickets |
So: the big win comes from removing the O(n^2) pattern; the redesign's extra win is smaller and only visible for many small tickets. If the redesign is not faster than plain StringBuilder for workload C, that is expected and will be explained.
