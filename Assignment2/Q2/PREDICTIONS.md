# Q2 – Predictions (written BEFORE running)

| Operation (reps per measurement = min(2000, n/2)) | ArrayList | LinkedList | ArrayDeque | plain array (manual shift) |
|---|---|---|---|---|
| add at end | O(1) amortised, fastest | O(1) but allocates a node each time (slower than ArrayList) | O(1) | O(1) amortised |
| insert/remove at front | O(n): grows linearly with size | O(1) | O(1) | O(n) (slower than ArrayList's arraycopy) |
| insert/remove in middle | O(n) shift (fast memmove) | O(n) pointer walk, cache-unfriendly - predicted SLOWER than ArrayList even though both are O(n) | not supported | O(n) |
| get k-th | O(1) | O(n) | not supported | O(1) |
| iterate all | fastest (contiguous) | slower (pointer chasing) | fast | fastest |

Prediction: ArrayList is a sound choice when the pattern is "append at the end, random access, iterate" and inserts/removals in the front/middle are rare. I would stop recommending it when front insert/remove become frequent (the "Up Next" queue) - then ArrayDeque, since it keeps O(1) at both ends without the node overhead of LinkedList. LinkedList should rarely win in practice because middle operations still need an O(n) walk.
