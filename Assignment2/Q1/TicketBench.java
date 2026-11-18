import java.lang.management.ManagementFactory;
import java.util.*;

/** Q1: String + vs StringBuilder vs StringBuffer (+ String.join / StringJoiner) under three workloads, then a threaded shared-log test. */
public class TicketBench {
    static final com.sun.management.ThreadMXBean TMX = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    static long sink;

    static String plus(String[] f)   { String s = ""; for (String x : f) s = s + x; return s; }
    static String sb(String[] f)     { StringBuilder b = new StringBuilder(); for (String x : f) b.append(x); return b.toString(); }
    static String sbuf(String[] f)   { StringBuffer b = new StringBuffer(); for (String x : f) b.append(x); return b.toString(); }
    static String join(String[] f)   { return String.join("", f); }
    static String joiner(String[] f) { StringJoiner j = new StringJoiner(""); for (String x : f) j.add(x); return j.toString(); }

    interface Build { String run(String[] f); }
    static long usedKB() { Runtime r = Runtime.getRuntime(); return (r.totalMemory() - r.freeMemory()) / 1024; }

    static String[] fields(int n) {
        String[] f = new String[n];
        for (int i = 0; i < n; i++) f[i] = "field" + i + "=Value" + ((i * 7919) % 1000) + "; ";
        return f;
    }

    static void run(String workload, int tickets, int nFields) {
        String[] f = fields(nFields);
        String[] names = {"String +", "StringBuilder", "StringBuffer", "String.join", "StringJoiner"};
        Build[] bs = { TicketBench::plus, TicketBench::sb, TicketBench::sbuf, TicketBench::join, TicketBench::joiner };
        long[] estObjs = { nFields, 1, 1, 1, 1 };   // intermediate Strings per ticket (estimated)
        for (int a = 0; a < bs.length; a++) {
            for (int w = 0; w < 2; w++) for (int t = 0; t < tickets; t++) sink += bs[a].run(f).length();   // warm-up
            int runs = 5; long totNs = 0, totAlloc = 0, heapDelta = 0;
            for (int r = 0; r < runs; r++) {
                System.gc();
                long h0 = usedKB(), a0 = TMX.getCurrentThreadAllocatedBytes(), t0 = System.nanoTime();
                for (int t = 0; t < tickets; t++) sink += bs[a].run(f).length();
                totNs += System.nanoTime() - t0; totAlloc += TMX.getCurrentThreadAllocatedBytes() - a0;
                heapDelta += usedKB() - h0;
            }
            double ms = totNs / (double) runs / 1e6;
            System.out.printf("%-34s %-14s %10.2f ms %12.3f us/ticket %12.1f KB/ticket %9d %12d%n", workload, names[a], ms,
                    ms * 1000 / tickets, totAlloc / (double) runs / tickets / 1024, estObjs[a], heapDelta / runs);
        }
    }

    /** Shared booking log appended by several threads. */
    static void threads() throws Exception {
        final int T = 8, N = 20_000; final String line = "booking-line;"; final int expected = T * N * line.length();
        String[] kinds = {"StringBuilder (unsynchronised)", "StringBuffer", "StringBuilder + synchronized block"};
        for (int k = 0; k < kinds.length; k++) {
            int correct = 0, crashed = 0; long totNs = 0; int minLen = Integer.MAX_VALUE;
            for (int trial = 0; trial < 5; trial++) {
                final StringBuilder sbd = new StringBuilder(); final StringBuffer sbf = new StringBuffer(); final Object lock = new Object();
                final int kind = k; final int[] errors = {0};
                Thread[] th = new Thread[T];
                long t0 = System.nanoTime();
                for (int i = 0; i < T; i++) {
                    th[i] = new Thread(() -> {
                        try { for (int j = 0; j < N; j++) {
                            if (kind == 0) sbd.append(line); else if (kind == 1) sbf.append(line); else synchronized (lock) { sbd.append(line); } } }
                        catch (RuntimeException e) { synchronized (errors) { errors[0]++; } }
                    });
                    th[i].start();
                }
                for (Thread t : th) t.join();
                totNs += System.nanoTime() - t0;
                int len = kind == 1 ? sbf.length() : sbd.length();
                if (len == expected && errors[0] == 0) correct++;
                if (errors[0] > 0) crashed++;
                minLen = Math.min(minLen, len);
            }
            System.out.printf("%-36s correct %d/5 trials, trials with exception %d, shortest length %d (expected %d), avg %.1f ms%n",
                    kinds[k], correct, crashed, minLen, expected, totNs / 5.0 / 1e6);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.printf("%-34s %-14s %13s %20s %19s %9s %12s%n", "Workload", "Approach", "total/run", "per ticket", "allocated", "est.objs", "heapDelta KB");
        run("A: 200,000 tickets x 10 fields", 200_000, 10);
        run("B: 10,000 tickets x 50 fields", 10_000, 50);
        run("C: 1 ticket x 20,000 fields (own)", 1, 20_000);
        System.out.println("\n--- Shared booking log, 8 threads x 20,000 appends ---");
        threads();
        // String.format for a single short ticket (readability vs speed)
        long t0 = System.nanoTime(); String s = "";
        for (int i = 0; i < 200_000; i++) s = String.format("PNR=%d;Name=%s;Coach=%s;Berth=%d;Fare=%.2f", i, "Arun", "S5", i % 72, 450.5);
        System.out.printf("%nString.format single ticket (5 fields): %.3f us/ticket  (sample: %s)%n", (System.nanoTime() - t0) / 200_000 / 1000.0, s);
        if (sink == 1) System.out.print("");
    }
}
