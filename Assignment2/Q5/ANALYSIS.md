# Q5 – A generic ranking component derived from a principle

Files: `LeaderboardDemo.java` (given code, TODO 1 & 2 completed), `RankingLab.java` (tests/experiments), `DESIGN_AND_PREDICTIONS.md` (written before implementing), `output_*.txt/.svg`.
Run: `javac -d out *.java && java -cp out LeaderboardDemo && java -cp out RankingLab`

## Complete-the-code result
Output: `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]` = expected output ✔.
**Why Bisons is above Panthers:** both have 9 points and goal difference 4, so the first two stages tie; the third stage compares names alphabetically and "Bisons" < "Panthers".
TODO 1: `new ArrayList<>(items)` then `copy.sort(order)` – sorts a copy so the stored data/insertion order is untouched. TODO 2: `comparingInt(points).reversed().thenComparing(comparingInt(goalDiff).reversed()).thenComparing(name)`.

## Design decisions (derived from the principle)
- **Comparator objects instead of `Comparable<Team>`:** a team has no single natural order; the leaderboard is one policy among several (fair-play table, alphabetical list). A Comparator keeps the policy outside the class, can be swapped without touching `Team`, and the same `Leaderboard<T>` ranks players (`Player` record, tested) or colleges.
- **Multi-field comparison:** a chain (`comparing`, `thenComparing`, `reversed`) is a lexicographic order. `Integer.compare` is used, never subtraction.
- **Generics:** `Leaderboard<T>` holds `Comparator<? super T>` (a `Comparator<Object>` can rank `Team`s – tested). `<T extends Comparable<? super T>> T maxOf(List<? extends T>)` shows a bounded type and a wildcard: `List<? extends T>` lets the method accept a `List<Integer>` or `List<String>` for reading.

## Why the comparator satisfies the principle (reasoning, not output)
Each stage compares one key with `Integer.compare`/`String.compareTo`, which is a total preorder (a strict weak ordering): antisymmetric, transitive, ties only when the keys are equal. `reversed()` of a strict weak ordering is one. The chain returns the result of the **first stage that does not tie**, i.e. compares the key tuple (−points, −goalDiff, name) lexicographically. Lexicographic composition preserves the properties: *antisymmetry* – the deciding stage is the same for (a,b) and (b,a) and flips sign; *transitivity* – if a>b is decided at stage i and b>c at stage j, then at stage min(i,j) a is strictly ahead of c (equal on all earlier stages by transitivity of equality, strictly ahead at that stage), so a>c; *equality* – the comparison returns 0 only when all three keys are equal, and such teams compare identically against every other team. Because name is the last stage, ties are only among teams with identical (points, goalDiff, name), so the ranking is deterministic and independent of input order.

## Experimental evidence
**Data set 1 (ties at every level):** result equals my hand-worked ranking; brute-force check over all triples: **0 antisymmetry, 0 transitivity, 0 equality-consistency violations**; reversed input gives identical output.
**Data set 2 + generated lists – deliberately faulty comparators:**
| Comparator | Observation |
|---|---|
| `b.points - a.points` (subtraction) | with `Integer.MAX_VALUE` and a negative score the subtraction overflows: **15 transitivity violations**, wrong order (Mid3, Normal, Mid2 instead of Mid3, Mid2, Normal; it also ignores goal difference) |
| ignores the name tie-breaker | no contract violation, but **output depends on input order** (Panthers/Bisons swap when the input is reversed) – not deterministic; the good comparator gives the same result both ways |
| "points within 1 are equal" | not a valid equivalence: **16 equality-consistency violations**; `List.sort` of 5,000 random teams throws `IllegalArgumentException: Comparison method violates its general contract!` |
(My prediction listed "transitivity violations" for the tolerant comparator; the checker reports them as equality-consistency violations – the same failure seen from the equality side.)

## Alternative concept – `Comparable<Team>` or three stable sorts
- With `Comparable<Team>` the same chain would live in `Team.compareTo`; it works with `Collections.sort(list)`/`TreeSet`, but fixes one order into the class and is harder to reuse for players/colleges (each needs its own `compareTo`).
- Three stable sorts (name, then goalDiff, then points – **reverse order of importance**): measured against one chain-comparator sort, with identical results (checked key by key):
| n teams | one chain sort | three sorts |
|---|---|---|
| 10,000 | 4.1 ms | 9.2 ms |
| 100,000 | 48.7 ms | 68.5 ms |
| 500,000 | 338 ms | 452 ms |
Complexity: 1 × O(n log n) vs 3 × O(n log n) (same Big-O, ≈ 1.3–2.2x measured – less than 3x because single-key comparisons are cheaper and TimSort exploits runs left by earlier passes). Extra space: `List.sort`/TimSort needs a temporary array of up to n/2 references (O(n)) in either approach; three sorts reuse it sequentially, so peak extra space is the same. Correctness risk: three sorts only work with a **stable** sort and the passes in the right order; the comparator chain is easier to reason about and to reuse for players and colleges.
