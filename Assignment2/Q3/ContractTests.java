import java.util.*;

/** Q3: three deliberately designed tests of the equals()/hashCode() contract, with predictions printed next to observations. */
public class ContractTests {
    static class Broken {            // equals by regNo, NO hashCode  (the junior developer's class)
        final String regNo; final String name;
        Broken(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof Broken b && regNo.equals(b.regNo); }
    }
    static class Fixed {             // corrected class
        final String regNo; final String name;
        Fixed(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof Fixed f && regNo.equals(f.regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
    }
    static class Mutable {           // hashCode uses a field that can change
        String regNo; Mutable(String r) { regNo = r; }
        @Override public boolean equals(Object o) { return o instanceof Mutable m && regNo.equals(m.regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
    }
    static void row(String test, Object predicted, Object observed) {
        System.out.printf("%-62s predicted=%-6s observed=%-6s %s%n", test, predicted, observed, String.valueOf(predicted).equals(String.valueOf(observed)) ? "MATCH" : "DIFFERENT");
    }

    public static void main(String[] args) {
        System.out.println("=== Test 1: scans that appear to work (same object scanned twice) ===");
        Broken b1 = new Broken("192311001", "Arun"), b2 = new Broken("192311002", "Divya");
        Set<Broken> s1 = new HashSet<>(List.of(b1, b2)); s1.add(b1);
        row("Broken class, same reference re-added -> size", 2, s1.size());

        System.out.println("\n=== Test 2: duplicates deliberately exposed (different objects, same regNo) ===");
        Set<Broken> s2 = new HashSet<>();
        for (int gate = 1; gate <= 3; gate++) s2.add(new Broken("192311001", "Arun"));
        row("Broken class, 3 scans of the same student -> size", 3, s2.size());
        row("   equals() of two scans (the method itself is right)", true, new Broken("1", "A").equals(new Broken("1", "A")));
        row("   same hashCode?", false, new Broken("1", "A").hashCode() == new Broken("1", "A").hashCode());
        Set<Fixed> s2f = new HashSet<>();
        for (int gate = 1; gate <= 3; gate++) s2f.add(new Fixed("192311001", "Arun"));
        row("Fixed class, 3 scans of the same student -> size", 1, s2f.size());

        System.out.println("\n=== Test 3a (own test): change a field used by hashCode() after insertion ===");
        Mutable m = new Mutable("192311001");
        Set<Mutable> s3 = new HashSet<>(); s3.add(m);
        m.regNo = "192311999";
        row("size after mutation", 1, s3.size());
        row("contains(m) (same object!)", false, s3.contains(m));
        row("contains(new Mutable(\"192311001\")) (original key)", false, s3.contains(new Mutable("192311001")));
        row("remove(m)", false, s3.remove(m));
        row("size after failed remove (entry is lost/leaked)", 1, s3.size());

        System.out.println("\n=== Test 3b (own test): TreeSet comparator inconsistent with equals ===");
        Fixed x = new Fixed("192311001", "Arun"), y = new Fixed("192311002", "Arun");
        Set<Fixed> hs = new HashSet<>(List.of(x, y));
        Set<Fixed> ts = new TreeSet<>(Comparator.comparing((Fixed f) -> f.name));   // compares name only
        ts.add(x); ts.add(y);
        row("HashSet size (equals by regNo)", 2, hs.size());
        row("TreeSet(compare by name) size", 1, ts.size());

        System.out.println("\n=== Alternative structures: cost per scan for n unique students (50% repeated scans) ===");
        System.out.printf("%-10s %16s %16s %16s%n", "n unique", "HashSet ns/scan", "TreeSet ns/scan", "HashMap ns/scan");
        for (int n : new int[]{1_000, 10_000, 100_000, 500_000}) {
            int scans = 2 * n; String[] regs = new String[scans]; Random r = new Random(1);
            for (int i = 0; i < scans; i++) regs[i] = "1923" + (100000 + r.nextInt(n));
            double hs1 = Double.MAX_VALUE, ts1 = hs1, hm1 = hs1; int uniq = 0;
            for (int trial = 0; trial < 3; trial++) {
                Set<Fixed> a = new HashSet<>(); long t0 = System.nanoTime();
                for (String s : regs) a.add(new Fixed(s, "x")); hs1 = Math.min(hs1, (System.nanoTime() - t0) / (double) scans); uniq = a.size();
                Set<Fixed> b = new TreeSet<>(Comparator.comparing((Fixed f) -> f.regNo)); t0 = System.nanoTime();
                for (String s : regs) b.add(new Fixed(s, "x")); ts1 = Math.min(ts1, (System.nanoTime() - t0) / (double) scans);
                Map<String, Fixed> c = new HashMap<>(); t0 = System.nanoTime();
                for (String s : regs) c.putIfAbsent(s, new Fixed(s, "x")); hm1 = Math.min(hm1, (System.nanoTime() - t0) / (double) scans);
            }
            System.out.printf("%-10d %16.1f %16.1f %16.1f   (distinct found: %d)%n", n, hs1, ts1, hm1, uniq);
        }
    }
}
