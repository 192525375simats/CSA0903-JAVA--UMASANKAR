# Q2 – When is ArrayList an appropriate engineering choice?

Files: `ListBench.java`, `PREDICTIONS.md` (before running), `output.txt/.svg`.
Run: `javac -d out ListBench.java && java -Xms1g -Xmx3g -cp out ListBench`

## Experiment design
Sizes n = 1,000 / 10,000 / 100,000. Structures: ArrayList, LinkedList, ArrayDeque (front/end only), and a plain `Object[]` with **manual** element shifting. Each operation is repeated min(2000, n/2) times (add-at-end: n adds; iterate: 5 full passes) on a fresh, pre-filled list; one warm-up trial + median of 3 timed trials; result = ns per operation. (n = 1,000 numbers are small/noisy: JIT warm-up and nanosecond-scale timers.)

## Measured results (ns/op, n = 100,000)
| Operation | ArrayList | LinkedList | ArrayDeque | plain array (manual) |
|---|---|---|---|---|
| add at end | 9.4 | 14.5 | 10.4 | 9.2 |
| insert at front | 7,928 | 15.6 | 11.6 | 185,383 |
| insert in middle | 3,917 | 82,832 | n/a | 93,607 |
| remove at front | 7,425 | 9.3 | 5.3 | 162,538 |
| remove in middle | 4,206 | 79,767 | n/a | 101,087 |
| get k-th | 2.6 | 80,482 | n/a | 2.4 |
| iterate (per element) | 1.5 | 4.7 | 0.6 | 0.6 |
Scaling check (ArrayList insert at front): 454 ns (10k) → 7,928 ns (100k): ×17.5 for ×10 size ≈ linear (O(n)). LinkedList get(k): 7,762 → 80,482 ns: ×10.4 ≈ linear walk. ArrayDeque front ops stay ≈ 10 ns at every size (O(1)).

## Prediction check
Supported: ArrayList fastest for get/iterate/middle ops; LinkedList wins only at the front; LinkedList middle insert is **21x slower than ArrayList's even though both are O(n)** (pointer chasing vs one `memmove`); manual shifting is **23x slower than ArrayList's `System.arraycopy`** (same O(n)). Slightly different from my prediction: LinkedList add-at-end was only ~1.5x slower than ArrayList (node allocation is cheap in the young generation).

## Answer: under what pattern is ArrayList reasonable, and when do I stop recommending it?
ArrayList is a sound default when the playlist is mostly **appended at the end, accessed by index and iterated**, and front/middle inserts/removals are occasional. At n ≤ 10,000 even a front insert costs only ~0.5 µs, which is invisible to a user. I stop recommending it when **inserts/removals at the front (or middle) become frequent and n is large (≳ 10⁴–10⁵)**: at n = 100,000 each front operation costs ~8 µs (×1,000 per second = 8 ms/s of pure shifting, and it grows linearly with the playlist). LinkedList is *not* the fix for middle edits: it still needs an O(n) walk and was 21x slower in the middle.

## Changed condition – "Up Next" queue (add/remove at both ends)
Workload: 30,000 × {addLast, addFirst, removeFirst}.
| n | ArrayList | LinkedList | ArrayDeque | plain array |
|---|---|---|---|---|
| 1,000 | 46 ms | 1.1 ms | 0.60 ms | 1,730 ms |
| 10,000 | 81 ms | 1.1 ms | 0.61 ms | 2,805 ms |
| 100,000 | **528 ms** | 1.1 ms | **0.90 ms** | 13,142 ms |
**ArrayDeque is the right structure for the Up Next queue** (590x faster than ArrayList at 100k, and ~1.2x faster than LinkedList with no per-node object). The original ArrayList recommendation stays valid for the *playlist* (random access/iteration) – the two structures serve two different usage patterns, so the app should use both.

## Complexity and space table
| Operation | ArrayList | LinkedList | ArrayDeque | plain array (manual shift) |
|---|---|---|---|---|
| add at end | O(1) amortised | O(1) | O(1) amortised | O(1) amortised |
| insert at front | O(n) | O(1) | O(1) | O(n) |
| insert in middle | O(n) (memmove) | O(n) walk + O(1) link | – | O(n) |
| get k-th | O(1) | O(n) | – | O(1) |
| iterate | O(n), cache friendly | O(n), pointer chasing | O(n) | O(n) |
| space / element | 1 reference (4–8 B) + up to 50% spare capacity | 1 node ≈ 24–40 B (header + prev + next + item) | 1 slot + up to 100% spare (power of 2) | 1 reference + spare |
(space figures are typical HotSpot 64-bit values, not measured here)

**Plain array with manual shifting:** same asymptotic table as ArrayList (O(n) shifting, O(1) get) but you must write growth and shifting yourself; measured 23x slower than `arraycopy`. It only makes sense for fixed-size or primitive data.
