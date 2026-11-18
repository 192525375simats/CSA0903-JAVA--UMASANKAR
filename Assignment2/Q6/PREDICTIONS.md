# Q6 – Design and predictions (written BEFORE running)

Why a plain Queue is not enough: FIFO serves strictly by arrival; a severity-1 patient arriving after ten severity-5 patients would wait behind them.
Design: `PriorityQueue<Patient>` with comparator (severity ascending, then arrival counter ascending). The monotonically increasing arrival counter is the tie-breaker; PriorityQueue is NOT stable by itself (equal elements come out in unspecified order), so the counter makes the order total and deterministic.
Why printing a PriorityQueue is not treatment order: it is a binary heap in an array; toString/iterator walk the array in heap layout (only parent <= children is guaranteed), so only element 0 is guaranteed to be next. Treatment order is obtained only by repeated poll().

Predictions
- admit (offer): O(log n) worst case, but average O(1) sift-up for random keys -> ns per admit nearly flat with n (slightly rising).
- next (poll): O(log n) (sift-down) -> ns per poll rises slowly with n (about +1 step per doubling), roughly 2-4x from 1,000 to 1,000,000 mostly from cache misses.
- Starvation: with continuous arrivals of severity 1-2 at a rate >= service rate, a severity-5 patient is never at the head -> unbounded wait.
- Aging rule: effective priority = arrival + W*severity (equivalently a patient gains one severity level every W minutes). Time-independent key => still a plain PriorityQueue, no re-heapifying. Predicted bound on the wait of a severity-5 patient: about 4W (= (5-1)*W) after arrival when load is saturated.
- 5,000 patients/day: total CPU cost of admit+poll of 5,000 patients expected far below 10 ms -> data structure is not the bottleneck.
- Alternatives: sorted LinkedList: admit O(n), serve O(1); five FIFO queues: admit O(1), serve O(1) (scan 5 queues) - but cannot express aging/finer priority.
