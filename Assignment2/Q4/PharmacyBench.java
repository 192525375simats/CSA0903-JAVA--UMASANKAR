import java.util.*;

/** Q4: HashMap vs TreeMap vs LinkedHashMap (vs Hashtable and sorted array + binarySearch) for a medicine inventory. */
public class PharmacyBench {
    record Medicine(String code, int qty) {}
    static long sink;

    static String[] codes(int n, long seed) {
        List<Integer> ids = new ArrayList<>(); for (int i = 0; i < n; i++) ids.add(i * 7 + 1);
        Collections.shuffle(ids, new Random(seed));            // insertion order != code order
        String[] c = new String[n]; for (int i = 0; i < n; i++) c[i] = String.format("MED%08d", ids.get(i)); return c;
    }

    static double ms(long ns) { return ns / 1e6; }

    public static void main(String[] args) {
        System.out.printf("%-9s %-14s %10s %14s %12s %18s %18s %s%n", "n", "structure", "put all ms", "100k gets ms", "ns/get", "code-order rpt ms", "insert-order rpt ms", "order check");
        for (int n : new int[]{1_000, 10_000, 100_000, 500_000}) {
            String[] keys = codes(n, 42);
            String[] sorted = keys.clone(); Arrays.sort(sorted);
            Random rnd = new Random(7); String[] probe = new String[100_000];
            for (int i = 0; i < probe.length; i++) probe[i] = keys[rnd.nextInt(n)];
            String[] names = {"HashMap", "TreeMap", "LinkedHashMap", "Hashtable"};
            for (int k = 0; k < names.length; k++) {
                double bestPut = 1e18, bestGet = 1e18, bestCode = 1e18, bestIns = 1e18; String check = "";
                for (int trial = 0; trial < 3; trial++) {
                    Map<String, Medicine> m = switch (k) { case 0 -> new HashMap<>(); case 1 -> new TreeMap<>(); case 2 -> new LinkedHashMap<>(); default -> new Hashtable<>(); };
                    long t0 = System.nanoTime();
                    for (String c : keys) m.put(c, new Medicine(c, 10));
                    bestPut = Math.min(bestPut, ms(System.nanoTime() - t0));
                    t0 = System.nanoTime();
                    for (String p : probe) sink += m.get(p).qty();
                    bestGet = Math.min(bestGet, ms(System.nanoTime() - t0));
                    // report 1: all medicines in CODE order
                    t0 = System.nanoTime(); List<String> codeRpt = new ArrayList<>(n);
                    if (k == 1) codeRpt.addAll(m.keySet());                       // TreeMap: already sorted
                    else { codeRpt.addAll(m.keySet()); Collections.sort(codeRpt); } // others: extra O(n log n) sort
                    bestCode = Math.min(bestCode, ms(System.nanoTime() - t0));
                    boolean codeOk = codeRpt.equals(Arrays.asList(sorted));
                    // report 2: medicines in INSERTION order
                    t0 = System.nanoTime(); List<String> insRpt = new ArrayList<>(n);
                    boolean insNatural = k == 2;
                    if (insNatural) insRpt.addAll(m.keySet());
                    else { for (String c : keys) insRpt.add(c); }                  // others: need a separately kept list (extra O(n) memory)
                    bestIns = Math.min(bestIns, ms(System.nanoTime() - t0));
                    boolean insOk = insRpt.equals(Arrays.asList(keys));
                    boolean rawCodeOrder = new ArrayList<>(m.keySet()).equals(Arrays.asList(sorted));
                    boolean rawInsOrder = new ArrayList<>(m.keySet()).equals(Arrays.asList(keys));
                    check = "iteration order natural? code=" + rawCodeOrder + " insertion=" + rawInsOrder + " | reports correct: " + (codeOk && insOk);
                }
                System.out.printf("%-9d %-14s %10.2f %14.2f %12.1f %18.2f %18.2f %s%n", n, names[k], bestPut, bestGet, bestGet * 1e6 / 100_000, bestCode, bestIns, check);
            }
            // alternative: sorted array + binarySearch
            Medicine[] arr = new Medicine[n]; String[] ak = new String[n]; int sz = 0; long t0 = System.nanoTime();
            if (n <= 100_000) {
                for (String c : keys) { int pos = Arrays.binarySearch(ak, 0, sz, c); pos = -pos - 1;
                    System.arraycopy(ak, pos, ak, pos + 1, sz - pos); System.arraycopy(arr, pos, arr, pos + 1, sz - pos); ak[pos] = c; arr[pos] = new Medicine(c, 10); sz++; }
            } else { ak = sorted; arr = new Medicine[n]; sz = n; }
            double putMs = ms(System.nanoTime() - t0);
            t0 = System.nanoTime();
            for (String p : probe) sink += arr[Arrays.binarySearch(ak, 0, sz, p)] == null ? 0 : 1;
            double getMs = ms(System.nanoTime() - t0);
            System.out.printf("%-9d %-14s %10s %14.2f %12.1f %18s %18s %s%n", n, "sorted array", n <= 100_000 ? String.format("%.2f", putMs) : "skipped", getMs, getMs * 1e6 / 100_000, "0 (already sorted)", "needs side list", "insert = O(n) shift each");
        }
        // memory per entry (approximate): heap delta after building 200k entries
        System.out.println("\nApproximate heap per entry (200,000 entries, after GC):");
        String[] keys = codes(200_000, 1);
        for (String nm : new String[]{"HashMap", "TreeMap", "LinkedHashMap"}) {
            System.gc(); long b = used(); Map<String, Medicine> m = nm.equals("HashMap") ? new HashMap<>() : nm.equals("TreeMap") ? new TreeMap<>() : new LinkedHashMap<>();
            for (String c : keys) m.put(c, new Medicine(c, 1));
            System.gc(); long a = used(); System.out.printf("  %-14s ~ %d bytes/entry (includes shared key + value objects)%n", nm, (a - b) / 200_000); sink += m.size();
        }
        Medicine[] arr = new Medicine[200_000]; System.gc(); long b = used(); for (int i = 0; i < arr.length; i++) arr[i] = new Medicine(keys[i], 1); System.gc();
        System.out.printf("  %-14s ~ %d bytes/entry (value objects only; keys shared)%n", "array of values", (used() - b) / 200_000); sink += arr.length;
        if (sink == 1) System.out.print("");
    }
    static long used() { Runtime r = Runtime.getRuntime(); return r.totalMemory() - r.freeMemory(); }
}
