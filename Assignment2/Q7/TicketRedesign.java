import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.*;

/** Q7: diagnose the O(n^2) '+' ticket builder (from Q1), redesign it, and compare original vs plain StringBuilder vs redesign under identical conditions. */
public class TicketRedesign {
    static final com.sun.management.ThreadMXBean TMX = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    static long sink;

    // ----- ORIGINAL: s = s + ... in a loop -----
    static String original(String pnr, String[] names, int[] vals) {
        String s = "PNR:" + pnr + "\n";
        for (int i = 0; i < vals.length; i++) s = s + names[i] + "=" + vals[i] + "; ";
        return s + "\n-- end --";
    }
    // ----- BASELINE: plain StringBuilder created per ticket (the Q1 answer) -----
    static String baseline(String pnr, String[] names, int[] vals) {
        StringBuilder sb = new StringBuilder();
        sb.append("PNR:").append(pnr).append('\n');
        for (int i = 0; i < vals.length; i++) sb.append(names[i]).append('=').append(vals[i]).append("; ");
        return sb.append("\n-- end --").toString();
    }
    // ----- REDESIGN: reusable, pre-sized, thread-confined formatter with precomputed labels -----
    static final class TicketFormatter {
        private static final String HEAD = "PNR:", TAIL = "\n-- end --", SEP = "; ";
        private final StringBuilder buf = new StringBuilder(256);
        private final String[] labels;                 // "name=" built once per ticket TYPE, not per ticket
        private int capacityHint = 256;                // remembers the largest ticket seen
        TicketFormatter(String[] names) { labels = new String[names.length]; for (int i = 0; i < names.length; i++) labels[i] = names[i] + "="; }
        String format(String pnr, int[] vals) {
            buf.setLength(0);                           // reuse: no new builder, no regrowth after the first big ticket
            buf.ensureCapacity(capacityHint);
            buf.append(HEAD).append(pnr).append('\n');
            for (int i = 0; i < vals.length; i++) buf.append(labels[i]).append(vals[i]).append(SEP);
            buf.append(TAIL);
            if (buf.length() > capacityHint) capacityHint = buf.length();
            return buf.toString();                      // the ONLY object created per ticket
        }
    }
    static final ThreadLocal<TicketFormatter[]> TL = new ThreadLocal<>();   // (used in the concurrency check)

    interface Gen { String make(int t); }

    static void run(String label, int tickets, int nFields) {
        String[] names = new String[nFields]; int[] vals = new int[nFields];
        for (int i = 0; i < nFields; i++) { names[i] = "field" + i; vals[i] = (i * 7919) % 1000; }
        TicketFormatter fmt = new TicketFormatter(names);
        String[] vers = {"original (+)", "plain StringBuilder", "redesign (reuse)"};
        Gen[] gens = { t -> original("P" + t, names, vals), t -> baseline("P" + t, names, vals), t -> fmt.format("P" + t, vals) };
        // correctness first: byte-identical output
        boolean same = true; for (int t = 0; t < Math.min(tickets, 50); t++) same &= gens[0].make(t).equals(gens[1].make(t)) && gens[1].make(t).equals(gens[2].make(t));
        int len = gens[2].make(0).length();
        long copiedOriginal = 0, cum = ("PNR:P0\n").length();     // exact characters copied by the + chain (one copy of the whole prefix per iteration)
        for (int i = 0; i < nFields; i++) { cum += (names[i] + "=" + vals[i] + "; ").length(); copiedOriginal += cum; }
        System.out.printf("%n%s   (ticket length %d chars; outputs identical across versions: %s)%n", label, len, same);
        System.out.printf("  %-20s %10s %14s %15s %12s %22s%n", "version", "total ms", "us/ticket", "alloc KB/ticket", "String objs", "chars copied/ticket");
        double[] ms = new double[3];
        for (int v = 0; v < 3; v++) {
            for (int w = 0; w < 2; w++) for (int t = 0; t < tickets; t++) sink += gens[v].make(t).length();
            int runs = 5; long ns = 0, alloc = 0;
            for (int r = 0; r < runs; r++) { System.gc(); long a0 = TMX.getCurrentThreadAllocatedBytes(), t0 = System.nanoTime();
                for (int t = 0; t < tickets; t++) sink += gens[v].make(t).length(); ns += System.nanoTime() - t0; alloc += TMX.getCurrentThreadAllocatedBytes() - a0; }
            ms[v] = ns / (double) runs / 1e6;
            String objs = v == 0 ? "~" + (nFields + 2) : "1 (+PNR)";
            String copied = v == 0 ? String.valueOf(copiedOriginal) : v == 1 ? "<= ~" + (3L * len) + " (append+growth+toString)" : "~" + (2L * len) + " (append+toString)";
            System.out.printf("  %-20s %10.2f %14.3f %15.2f %12s %22s%n", vers[v], ms[v], ms[v] * 1000 / tickets, alloc / (double) runs / tickets / 1024, objs, copied);
        }
        System.out.printf("  speed-up: baseline vs original = %.1fx, redesign vs original = %.1fx, redesign vs baseline = %.2fx%n", ms[0] / ms[1], ms[0] / ms[2], ms[1] / ms[2]);
    }

    /** thread-confinement check: each worker owns its formatter -> no sharing, no locks; result must equal the single-threaded checksum */
    static void concurrency() throws Exception {
        int nFields = 50, perThread = 5000, T = 4; String[] names = new String[nFields]; int[] vals = new int[nFields];
        for (int i = 0; i < nFields; i++) { names[i] = "field" + i; vals[i] = (i * 7919) % 1000; }
        long expected = 0; for (int t = 0; t < perThread; t++) expected += baseline("P" + t, names, vals).hashCode();
        long[] got = new long[T]; Thread[] th = new Thread[T];
        for (int k = 0; k < T; k++) { final int id = k; th[k] = new Thread(() -> { TicketFormatter f = new TicketFormatter(names); long h = 0;   // one formatter per thread
            for (int t = 0; t < perThread; t++) h += f.format("P" + t, vals).hashCode(); got[id] = h; }); th[k].start(); }
        for (Thread t : th) t.join();
        boolean ok = true; for (long g : got) ok &= g == expected;
        System.out.println("\nConcurrency check: " + T + " threads x " + perThread + " tickets, one formatter per thread (no locks): all checksums equal single-thread result = " + ok);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Q7 Original vs Redesign under identical test conditions (5 timed runs after 2 warm-ups, averaged) ===");
        run("Workload B: 10,000 tickets x 50 fields", 10_000, 50);
        run("Workload C: 1 ticket x 20,000 fields", 1, 20_000);
        run("Workload D: 100,000 tickets x 10 fields", 100_000, 10);
        run("Workload E (scaling): 1 ticket x 5,000 fields", 1, 5_000);
        run("Workload F (scaling): 1 ticket x 10,000 fields", 1, 10_000);
        concurrency();
        if (sink == 1) System.out.print("");
    }
}
