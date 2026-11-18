import java.util.*;

/** Q5 lab: correct comparator vs deliberately faulty ones; property checker; reuse for players; Collections.sort vs three stable sorts. */
public class RankingLab {
    static final Comparator<Team> GOOD = Comparator.comparingInt((Team t) -> t.points).reversed()
            .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed()).thenComparing(t -> t.name);
    static final Comparator<Team> BAD_SUBTRACT = (a, b) -> b.points - a.points;                         // overflow-prone, also ignores tie-breakers
    static final Comparator<Team> BAD_NO_TIEBREAK = Comparator.comparingInt((Team t) -> t.points).reversed()
            .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed());                // ignores name
    static final Comparator<Team> BAD_TOLERANT = (a, b) -> Math.abs(a.points - b.points) <= 1 ? 0 : Integer.compare(b.points, a.points); // not transitive

    /** brute-force check of the principle over all pairs/triples. Returns counts of violations. */
    static String check(List<Team> xs, Comparator<Team> c) {
        int anti = 0, trans = 0, eq = 0;
        for (Team a : xs) for (Team b : xs) {
            if (Integer.signum(c.compare(a, b)) != -Integer.signum(c.compare(b, a))) anti++;
            for (Team x : xs) {
                if (c.compare(a, b) > 0 && c.compare(b, x) > 0 && c.compare(a, x) <= 0) trans++;
                if (c.compare(a, b) == 0 && Integer.signum(c.compare(a, x)) != Integer.signum(c.compare(b, x))) eq++;
            }
        }
        return "antisymmetry violations=" + anti + ", transitivity violations=" + trans + ", equality-consistency violations=" + eq;
    }
    static List<Team> sorted(List<Team> in, Comparator<Team> c) { List<Team> l = new ArrayList<>(in); l.sort(c); return l; }

    // reuse: the same Leaderboard<T> for a completely different type
    record Player(String name, int goals) {}

    public static void main(String[] args) {
        // ---------- data set 1: several ties at each level ----------
        List<Team> d1 = new ArrayList<>(List.of(new Team("Panthers", 9, 4), new Team("Bisons", 9, 4), new Team("Eagles", 9, 6), new Team("Falcons", 9, 6),
                new Team("Cobras", 6, 1), new Team("Anacondas", 6, 1), new Team("Lions", 6, 3), new Team("Zebras", 3, 0)));
        String expected = "[Eagles(9,6), Falcons(9,6), Bisons(9,4), Panthers(9,4), Lions(6,3), Anacondas(6,1), Cobras(6,1), Zebras(3,0)]";
        System.out.println("=== Data set 1 (ties at every level) ===");
        System.out.println("hand-worked expected: " + expected);
        System.out.println("GOOD comparator     : " + sorted(d1, GOOD));
        System.out.println("matches hand result : " + sorted(d1, GOOD).toString().equals(expected));
        System.out.println("GOOD property check : " + check(d1, GOOD));
        List<Team> rev = new ArrayList<>(d1); Collections.reverse(rev);
        System.out.println("same result for reversed input? " + sorted(rev, GOOD).equals(sorted(d1, GOOD)));
        Leaderboard<Team> lb = new Leaderboard<>(GOOD); d1.forEach(lb::add); lb.ranking();
        System.out.println("Leaderboard keeps insertion order of its stored data (ranking() sorts a copy): first stored = " + d1.get(0));

        // ---------- data set 2: designed to break faulty comparators ----------
        List<Team> d2 = new ArrayList<>(List.of(new Team("Max", Integer.MAX_VALUE, 0), new Team("Normal", 5, 0), new Team("Penalised", -10, 0), new Team("Mid", 4, 9),
                new Team("Mid2", 5, 1), new Team("Mid3", 6, 2), new Team("Low", 3, 0)));
        System.out.println("\n=== Data set 2 (designed to break faulty comparators) ===");
        System.out.println("GOOD            : " + sorted(d2, GOOD).stream().map(t -> t.name).toList() + "  | " + check(d2, GOOD));
        System.out.println("BAD_SUBTRACT    : " + sorted(d2, BAD_SUBTRACT).stream().map(t -> t.name).toList() + "  | " + check(d2, BAD_SUBTRACT));
        List<Team> d2r = new ArrayList<>(d2); Collections.reverse(d2r);
        System.out.println("BAD_TOLERANT    : " + check(d2, BAD_TOLERANT));
        System.out.println("BAD_NO_TIEBREAK on d1 input order : " + sorted(d1, BAD_NO_TIEBREAK));
        System.out.println("BAD_NO_TIEBREAK on d1 reversed    : " + sorted(rev, BAD_NO_TIEBREAK));
        System.out.println("  -> same output regardless of input order? " + sorted(d1, BAD_NO_TIEBREAK).equals(sorted(rev, BAD_NO_TIEBREAK)) + "  (GOOD: " + sorted(d1, GOOD).equals(sorted(rev, GOOD)) + ")");
        // large random list with the non-transitive comparator
        Random r = new Random(3); List<Team> big = new ArrayList<>();
        for (int i = 0; i < 5000; i++) big.add(new Team("T" + i, r.nextInt(12), r.nextInt(20)));
        try { big.sort(BAD_TOLERANT); System.out.println("BAD_TOLERANT sort of 5000 random teams: finished without exception (but order is not trustworthy)"); }
        catch (IllegalArgumentException e) { System.out.println("BAD_TOLERANT sort of 5000 random teams: IllegalArgumentException: " + e.getMessage()); }

        // ---------- reuse for players with a bounded generic helper and wildcards ----------
        Leaderboard<Player> pb = new Leaderboard<>(Comparator.comparingInt(Player::goals).reversed().thenComparing(Player::name));
        pb.add(new Player("Zoya", 7)); pb.add(new Player("Ali", 7)); pb.add(new Player("Ben", 9));
        System.out.println("\nReuse for players: " + pb.ranking());
        Comparator<Object> byToString = Comparator.comparing(Object::toString);          // Comparator<Object> accepted as Comparator<? super Team>
        Leaderboard<Team> wild = new Leaderboard<>(byToString); d1.forEach(wild::add);
        System.out.println("Comparator<Object> used for Team (? super T): " + wild.ranking().subList(0, 2));
        System.out.println("max by natural order via bounded generic: " + maxOf(List.of(3, 9, 4)) + ", " + maxOf(List.of("pear", "apple")));

        // ---------- alternative: three stable sorts vs one comparator chain ----------
        System.out.println("\n=== One comparator chain vs three stable sorts (name, then goalDiff, then points) ===");
        System.out.printf("%-10s %16s %16s %s%n", "n teams", "chain ms (1 sort)", "3 sorts ms", "identical output?");
        for (int n : new int[]{10_000, 100_000, 500_000}) {
            Random rr = new Random(9); List<Team> in = new ArrayList<>();
            for (int i = 0; i < n; i++) in.add(new Team("Team" + rr.nextInt(50), rr.nextInt(31), rr.nextInt(41) - 20));
            double chain = 1e18, three = 1e18; List<Team> o1 = null, o2 = null;
            for (int trial = 0; trial < 5; trial++) {
                List<Team> a = new ArrayList<>(in); long t0 = System.nanoTime(); a.sort(GOOD); chain = Math.min(chain, (System.nanoTime() - t0) / 1e6); o1 = a;
                List<Team> b = new ArrayList<>(in); t0 = System.nanoTime();
                Collections.sort(b, Comparator.comparing((Team t) -> t.name));
                Collections.sort(b, Comparator.comparingInt((Team t) -> t.goalDiff).reversed());
                Collections.sort(b, Comparator.comparingInt((Team t) -> t.points).reversed());
                three = Math.min(three, (System.nanoTime() - t0) / 1e6); o2 = b;
            }
            // compare by key (ties among identical keys are indistinguishable)
            boolean same = true; for (int i = 0; i < n && same; i++) same = GOOD.compare(o1.get(i), o2.get(i)) == 0;
            System.out.printf("%-10d %16.1f %16.1f %s%n", n, chain, three, same);
        }
    }
    static <T extends Comparable<? super T>> T maxOf(List<? extends T> xs) { T best = null; for (T x : xs) if (best == null || x.compareTo(best) > 0) best = x; return best; }
}
