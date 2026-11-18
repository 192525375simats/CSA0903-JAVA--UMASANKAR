# Q5 – Design derived from the principle (written BEFORE implementation)

Principle: a comparison must be a **strict weak ordering**: antisymmetric (sgn cmp(a,b) = -sgn cmp(b,a)), transitive (a>b and b>c => a>c) and equality-consistent (cmp(a,b)=0 => a and b compare identically with every x).

Design decisions
1. **Comparator objects, not `Comparable<Team>`.** A team has no single "natural" order: the leaderboard order (points, goal-diff, name) is one *policy*; a fair-play table or an alphabetical list are others. Comparator keeps the policy outside `Team`, lets `Leaderboard<T>` be reused for players/colleges/quiz teams, and several policies can coexist.
2. **Multi-field comparison** = a chain: `comparingInt(points).reversed()` (higher first) `.thenComparing(comparingInt(goalDiff).reversed())` `.thenComparing(name)`. Each stage is used only if all previous stages tie, so the chain is lexicographic, hence transitive. Using `Integer.compare` (never subtraction) avoids overflow.
3. **Generic component**: `Leaderboard<T>` holds `Comparator<? super T>`; `? super T` (contravariance) lets a `Comparator<Object>` or `Comparator<Person>` rank `Student`s. `addAll(Collection<? extends T>)` (covariance) accepts a `List<Team>` or `List<ProTeam>`. For a natural-order variant: `<T extends Comparable<? super T>>`.
4. **ranking()** returns a sorted *copy* so the stored data and insertion order are not modified.

Predictions
- Dataset 1 (ties at every level) with the correct chain: zero property violations; deterministic output.
- Faulty comparator 1 (points by subtraction) on data containing Integer.MAX_VALUE and a negative value: overflow => wrong order, property violations.
- Faulty comparator 2 (ignores the name tie-breaker): no contract violation but the output depends on input order (not deterministic) - ties are "equal" yet distinct teams.
- Faulty comparator 3 (points within 1 treated as equal): not transitive; predicted property violations and, for a large random list, `IllegalArgumentException: Comparison method violates its general contract`.
- Three stable sorts vs one comparator chain: identical output; 3 sorts costs ~2-3x time (3 x O(n log n)) and needs a stable sort + correct order of passes (name, goalDiff, points).
