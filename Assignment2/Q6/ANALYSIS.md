# Q6 – Priority, order and fairness in a queue (hospital triage)

Files: `Triage.java` (given code, TODO 1 completed), `TriageLab.java` (experiments), `PREDICTIONS.md` (design + predictions before running), `output_*.txt/.svg`.
Run: `javac -d out *.java && java -cp out Triage && java -Xms1g -Xmx3g -cp out TriageLab`

## Complete-the-code result
`Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)` = expected output ✔.
TODO 1: `new PriorityQueue<>(Comparator.comparingInt((Patient p) -> p.severity).thenComparingLong(p -> p.arrival))`.
**Arrival counter:** every patient gets `counter++` at admission, so arrival numbers are unique and increasing. `PriorityQueue` is a binary heap and is *not* stable – among equal keys the order is unspecified – so equal-severity patients are ordered by this second comparator key (earlier arrival = smaller number = first). The comparison becomes a total order and the result is deterministic.

## Design answers
- **Why a plain Queue (LinkedList/ArrayDeque) is not sufficient:** it serves strictly FIFO; a severity-1 patient arriving behind severity-5 patients would wait for all of them.
- **PriorityQueue + Comparator:** comparator = (severity ↑, arrival ↑). `admit` = `add` (sift-up), next = `poll` (remove root, sift-down), waiting list = copy the queue and poll the copy (or sort a copy) – never print/iterate the queue itself.
- **Why printing a PriorityQueue shows the wrong order:** it is a heap stored in an array; `toString()`/iterator walk the array in heap layout where only "parent ≤ children" holds. Observed for 8 patients: `[Meena(S1), Kumar(S1), Lata(S2), Imran(S1), Ravi(S3), John(S3), Divya(S2), Asha(S5)]` – Lata(S2) before Imran(S1).

## Correctness check (8 patients, worked by hand)
Patients in arrival order: Ravi 3, Meena 1, John 3, Asha 5, Kumar 1, Lata 2, Divya 2, Imran 1 → by hand: S1 in arrival order (Meena, Kumar, Imran), S2 (Lata, Divya), S3 (Ravi, John), S5 (Asha).
Program: `Meena(S1) Kumar(S1) Imran(S1) Lata(S2) Divya(S2) Ravi(S3) John(S3) Asha(S5)` → **match: true**.

## Growth investigation (prediction in PREDICTIONS.md, measured here; best of 5)
| n | admit ns/op | next ns/op | log₂ n |
|---|---|---|---|
| 1,000 | 80.6 | 194 | 10.0 |
| 10,000 | 83.8 | 213 | 13.3 |
| 100,000 | 46.3 | 270 | 16.6 |
| 1,000,000 | 35.9 | 472 | 19.9 |
Prediction: admit ≈ flat (average-case O(1) sift-up for random severities), next grows O(log n). Observed: admit flat/slightly falling (JIT warm-up and better cache behaviour on later runs), **next grows 2.4x for a 1000x larger queue** – consistent with O(log n) (log₂ n doubles from 10 to 20) plus cache misses. Worst case for admit is still O(log n).

## Starvation and a fair rule
**Why starvation happens:** the comparator always puts a lower severity number first, so while higher-priority patients keep arriving at least as fast as the doctor treats them, a severity-5 patient is never at the head. Measured (backlog of 40 mild patients + one S5 at t=0, then one new S1/S2 per minute, one treatment per minute, 20,000 min): **the S5 patient was never served**.
**Fair rule – aging:** one severity level is "worth" W minutes of waiting. Effective priority of a patient at time t = severity − (t − arrival)/W. Comparing two patients, the common t cancels, so the time-independent key **arrival + W·severity** gives the same order at all times – a plain `PriorityQueue` suffices, with no re-heapifying (tie-breakers: severity, then arrival).
| policy | severity-5 patient | avg wait S1 | avg wait S2 |
|---|---|---|---|
| strict severity | never served in 20,000 min | 0.1 min | 80.8 min |
| aging, W = 30 min | **served after 149 min** | 26.1 min | 56.1 min |
Aging removes starvation (bounded wait; predicted ≈ 4W = 120 min under saturation, observed 149 because the 40-patient backlog had to clear first), and lowers S2 waits (80.8 → 56.1), **but critical (S1) patients now wait 26 min on average instead of 0.1** – aged S2 patients overtake new S1 arrivals. For an emergency department W = 30 is therefore too aggressive for the top level; a safer design is to apply aging only below a floor (a patient may be promoted to at most severity 2/3, never above true critical), or use a larger W for the upper levels, and to keep S1 strictly first. (This refinement is a proposed design; only W = 30 versus strict was measured.)

## Deployment decision – 5,000 patients a day?
Measured: **1.35 ms** (best of 30) for 5,000 admits + 5,000 treatments with the aging comparator; a day has 86,400,000 ms, and even the 1,000,000-patient queue costs only ≈ 0.5 µs per operation. **Yes – the data structure is not a limit; the design can serve a city hospital** (and 1,000x more). Remaining engineering needs, not measured here: `PriorityQueue` is **not thread-safe** (use a lock or `PriorityBlockingQueue` with the same comparator), persistence across restarts, and removing/updating a patient who leaves or changes severity (`remove(Object)` is O(n)).

## Alternative concept (measured, total ms to admit then serve n patients)
| n | PriorityQueue | sorted LinkedList | 5 FIFO ArrayDeques |
|---|---|---|---|
| 1,000 | 0.29 | 15.1 | 0.59 |
| 10,000 | 2.85 | 321 | 0.56 |
| 40,000 | 51.4 | **9,437** | 1.93 |
- **Sorted LinkedList** (insert at the correct position): admit **O(n)** walk, serve next **O(1)** (`removeFirst`); n admissions = O(n²) – 184x slower than PQ at 40,000. Space O(n) with 24+ B per node.
- **Five FIFO queues** (one per severity): admit **O(1)**, serve next **O(1)** (scan at most 5 queues; with k levels O(k)), space O(n) – fastest and trivially fair within a level, but cannot express finer or time-dependent priority such as aging without extra logic.
- **PriorityQueue:** admit O(log n) (O(1) average), serve O(log n), space O(n) in one array (4–8 B/ref) – best general choice when priority is not a tiny fixed set.
