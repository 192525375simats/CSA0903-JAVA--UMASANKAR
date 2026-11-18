import java.util.*;

/** Q2: ArrayList vs LinkedList vs ArrayDeque vs plain array with manual shifting, at several sizes. */
public class ListBench {
    static final Object SONG = new Object();
    static long sink;

    static abstract class Ds {
        abstract String name(); abstract void addLast(); abstract void addFirst(); abstract void addAt(int i);
        abstract void removeFirst(); abstract void removeAt(int i); abstract Object get(int i); abstract long iterate(); abstract int size();
        boolean indexed() { return true; }
    }
    static class AL extends Ds { ArrayList<Object> l = new ArrayList<>();
        String name() { return "ArrayList"; } void addLast() { l.add(SONG); } void addFirst() { l.add(0, SONG); } void addAt(int i) { l.add(i, SONG); }
        void removeFirst() { l.remove(0); } void removeAt(int i) { l.remove(i); } Object get(int i) { return l.get(i); }
        long iterate() { long c = 0; for (Object o : l) if (o != null) c++; return c; } int size() { return l.size(); } }
    static class LL extends Ds { LinkedList<Object> l = new LinkedList<>();
        String name() { return "LinkedList"; } void addLast() { l.add(SONG); } void addFirst() { l.add(0, SONG); } void addAt(int i) { l.add(i, SONG); }
        void removeFirst() { l.remove(0); } void removeAt(int i) { l.remove(i); } Object get(int i) { return l.get(i); }
        long iterate() { long c = 0; for (Object o : l) if (o != null) c++; return c; } int size() { return l.size(); } }
    static class AD extends Ds { ArrayDeque<Object> l = new ArrayDeque<>();
        String name() { return "ArrayDeque"; } boolean indexed() { return false; }
        void addLast() { l.addLast(SONG); } void addFirst() { l.addFirst(SONG); } void addAt(int i) { throw new UnsupportedOperationException(); }
        void removeFirst() { l.removeFirst(); } void removeAt(int i) { throw new UnsupportedOperationException(); } Object get(int i) { throw new UnsupportedOperationException(); }
        long iterate() { long c = 0; for (Object o : l) if (o != null) c++; return c; } int size() { return l.size(); } }
    static class MA extends Ds { Object[] a = new Object[16]; int n;
        String name() { return "plain array"; }
        void grow() { if (n == a.length) a = Arrays.copyOf(a, n * 2); }
        void addLast() { grow(); a[n++] = SONG; }
        void addFirst() { addAt(0); }
        void addAt(int i) { grow(); for (int k = n; k > i; k--) a[k] = a[k - 1]; a[i] = SONG; n++; }   // manual shifting
        void removeFirst() { removeAt(0); }
        void removeAt(int i) { for (int k = i; k < n - 1; k++) a[k] = a[k + 1]; a[--n] = null; }
        Object get(int i) { return a[i]; }
        long iterate() { long c = 0; for (int k = 0; k < n; k++) if (a[k] != null) c++; return c; } int size() { return n; } }

    static Ds make(int kind) { switch (kind) { case 0: return new AL(); case 1: return new LL(); case 2: return new AD(); default: return new MA(); } }
    static Ds filled(int kind, int n) { Ds d = make(kind); for (int i = 0; i < n; i++) d.addLast(); return d; }

    static final String[] OPS = {"add at end", "insert at front", "insert in middle", "remove at front", "remove in middle", "get k-th (middle)", "iterate whole list"};

    /** returns ns per operation (median of 3 trials after a warm-up trial); NaN when unsupported. */
    static double measure(int kind, int op, int n, int reps) {
        if (!make(kind).indexed() && (op == 2 || op == 4 || op == 5)) return Double.NaN;
        double[] res = new double[4];
        for (int trial = 0; trial < 4; trial++) {
            Ds d = op == 0 ? make(kind) : filled(kind, n);
            long t0, t1; int ops;
            switch (op) {
                case 0: t0 = System.nanoTime(); for (int i = 0; i < n; i++) d.addLast(); t1 = System.nanoTime(); ops = n; break;
                case 1: t0 = System.nanoTime(); for (int i = 0; i < reps; i++) d.addFirst(); t1 = System.nanoTime(); ops = reps; break;
                case 2: t0 = System.nanoTime(); for (int i = 0; i < reps; i++) d.addAt(d.size() / 2); t1 = System.nanoTime(); ops = reps; break;
                case 3: t0 = System.nanoTime(); for (int i = 0; i < reps; i++) d.removeFirst(); t1 = System.nanoTime(); ops = reps; break;
                case 4: t0 = System.nanoTime(); for (int i = 0; i < reps; i++) d.removeAt(d.size() / 2); t1 = System.nanoTime(); ops = reps; break;
                case 5: t0 = System.nanoTime(); for (int i = 0; i < reps; i++) sink += d.get(n / 2 + (i & 7)) == null ? 1 : 0; t1 = System.nanoTime(); ops = reps; break;
                default: t0 = System.nanoTime(); for (int i = 0; i < 5; i++) sink += d.iterate(); t1 = System.nanoTime(); ops = 5 * n; break;
            }
            res[trial] = (t1 - t0) / (double) ops;
        }
        double[] r = Arrays.copyOfRange(res, 1, 4); Arrays.sort(r); return r[1];
    }

    static String fmt(double v) { return Double.isNaN(v) ? "n/a" : String.format("%.1f", v); }

    public static void main(String[] args) {
        int[] sizes = {1_000, 10_000, 100_000};
        for (int n : sizes) {
            System.out.println("\n=== Playlist size n = " + n + "   (ns per operation, median of 3 trials) ===");
            System.out.printf("%-20s %14s %14s %14s %14s%n", "operation", "ArrayList", "LinkedList", "ArrayDeque", "plain array");
            for (int op = 0; op < OPS.length; op++) {
                System.out.printf("%-20s", OPS[op]);
                for (int kind = 0; kind < 4; kind++) System.out.printf(" %14s", fmt(measure(kind, op, n, Math.min(2000, n / 2))));
                System.out.println();
            }
        }
        System.out.println("\n=== Changed condition: 'Up Next' queue = repeat {addLast, addFirst, removeFirst} 30,000 times ===");
        System.out.printf("%-10s %14s %14s %14s %14s   (total ms)%n", "n", "ArrayList", "LinkedList", "ArrayDeque", "plain array");
        for (int n : sizes) {
            System.out.printf("%-10d", n);
            for (int kind = 0; kind < 4; kind++) {
                double best = Double.MAX_VALUE;
                for (int trial = 0; trial < 3; trial++) {
                    Ds d = filled(kind, n); long t0 = System.nanoTime();
                    for (int i = 0; i < 30_000; i++) { d.addLast(); d.addFirst(); d.removeFirst(); }
                    best = Math.min(best, (System.nanoTime() - t0) / 1e6);
                }
                System.out.printf(" %14.2f", best);
            }
            System.out.println();
        }
        if (sink == 1) System.out.print("");
    }
}
