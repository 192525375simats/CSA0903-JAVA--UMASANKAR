import java.util.*;

/** Q6 lab: hand-checked correctness, heap display, growth measurements, starvation + aging, deployment, alternatives. */
public class TriageLab {
    static final Comparator<Patient> STRICT = Comparator.comparingInt((Patient p) -> p.severity).thenComparingLong(p -> p.arrival);
    static final int W = 30;     // aging: one severity level is "worth" W minutes of waiting
    static final Comparator<Patient> AGING = Comparator.comparingLong((Patient p) -> p.arrival + (long) W * p.severity)
            .thenComparingInt(p -> p.severity).thenComparingLong(p -> p.arrival);
    static long sink;

    public static void main(String[] args) {
        // ---------- correctness check with 8 patients ----------
        String[] names = {"Ravi", "Meena", "John", "Asha", "Kumar", "Lata", "Divya", "Imran"}; int[] sev = {3, 1, 3, 5, 1, 2, 2, 1};
        PriorityQueue<Patient> pq = new PriorityQueue<>(STRICT);
        for (int i = 0; i < names.length; i++) pq.add(new Patient(names[i], sev[i], i));
        System.out.println("=== Correctness (8 patients, hand-worked) ===");
        System.out.println("PriorityQueue printed directly (heap layout, NOT treatment order): " + pq);
        StringBuilder sb = new StringBuilder(); Patient p; while ((p = pq.poll()) != null) sb.append(p).append(' ');
        String expected = "Meena(S1) Kumar(S1) Imran(S1) Lata(S2) Divya(S2) Ravi(S3) John(S3) Asha(S5)";
        System.out.println("hand-worked expected : " + expected);
        System.out.println("poll() order observed: " + sb.toString().trim());
        System.out.println("match: " + sb.toString().trim().equals(expected));

        // ---------- growth ----------
        System.out.println("\n=== Growth: cost of admit and next (poll) per patient, random severities ===");
        System.out.printf("%-10s %14s %14s %14s%n", "n", "admit ns/op", "next ns/op", "log2(n)");
        for (int n : new int[]{1_000, 10_000, 100_000, 1_000_000}) {
            Random r = new Random(5); int[] s = new int[n]; for (int i = 0; i < n; i++) s[i] = 1 + r.nextInt(5);
            double ba = 1e18, bn = 1e18;
            for (int trial = 0; trial < 5; trial++) {
                PriorityQueue<Patient> q = new PriorityQueue<>(STRICT); long t0 = System.nanoTime();
                for (int i = 0; i < n; i++) q.add(new Patient("P", s[i], i));
                ba = Math.min(ba, (System.nanoTime() - t0) / (double) n); t0 = System.nanoTime();
                while (!q.isEmpty()) sink += q.poll().arrival; bn = Math.min(bn, (System.nanoTime() - t0) / (double) n);
            }
            System.out.printf("%-10d %14.1f %14.1f %14.1f%n", n, ba, bn, Math.log(n) / Math.log(2));
        }

        // ---------- starvation and aging ----------
        // (first version of this experiment started with an EMPTY queue, so every severity-1/2 patient was treated at once and waits were 0 - uninformative;
        //  it now starts with a backlog of 40 waiting severity-1/2 patients so that waiting actually happens)
        System.out.println("\n=== Starvation: backlog of 40 severity-1/2 patients + 1 severity-5 patient at t=0; then 1 new severity-1/2 patient per minute, doctor treats 1 per minute, 20,000 min ===");
        System.out.printf("%-30s %-34s %18s %18s%n", "policy", "severity-5 patient", "avg wait sev-1", "avg wait sev-2");
        for (String policy : new String[]{"strict severity (no aging)", "aging W=" + W + " min per level"}) {
            Comparator<Patient> c = policy.startsWith("strict") ? STRICT : AGING;
            PriorityQueue<Patient> q = new PriorityQueue<>(c); Random r = new Random(11);
            for (int i = 0; i < 40; i++) q.add(new Patient("B" + i, 1 + r.nextInt(2), 0));
            Patient late = new Patient("Late", 5, 0); q.add(late);
            long waited = -1; long[] sum = new long[3]; int[] cnt = new int[3];
            for (long t = 1; t <= 20_000; t++) {
                q.add(new Patient("N" + t, 1 + r.nextInt(2), t));
                Patient served = q.poll();
                if (served == late) waited = t;
                else { sum[served.severity] += t - served.arrival; cnt[served.severity]++; }
            }
            System.out.printf("%-30s %-34s %15.1f min %15.1f min%n", policy,
                    waited < 0 ? "NEVER SERVED in 20,000 min" : "served after " + waited + " min",
                    cnt[1] == 0 ? 0.0 : sum[1] / (double) cnt[1], cnt[2] == 0 ? 0.0 : sum[2] / (double) cnt[2]);
        }

        // ---------- deployment ----------
        System.out.println("\n=== Deployment: one day = 5,000 patients (admit all, then treat all) ===");
        double best = 1e18;
        for (int trial = 0; trial < 30; trial++) {
            Random r = new Random(trial); PriorityQueue<Patient> q = new PriorityQueue<>(AGING); long t0 = System.nanoTime();
            for (int i = 0; i < 5000; i++) q.add(new Patient("P" + i, 1 + r.nextInt(5), i));
            while (!q.isEmpty()) sink += q.poll().severity; best = Math.min(best, (System.nanoTime() - t0) / 1e6);
        }
        System.out.printf("best of 30 runs: %.3f ms for 5,000 admits + 5,000 treatments (a day is 86,400,000 ms)%n", best);

        // ---------- alternatives ----------
        System.out.println("\n=== Alternatives: admit n patients, then serve all (ms total) ===");
        System.out.printf("%-8s %16s %22s %20s%n", "n", "PriorityQueue", "sorted LinkedList", "5 FIFO ArrayDeques");
        for (int n : new int[]{1_000, 10_000, 40_000}) {
            Random r = new Random(2); int[] s = new int[n]; for (int i = 0; i < n; i++) s[i] = 1 + r.nextInt(5);
            long t0 = System.nanoTime(); PriorityQueue<Patient> q = new PriorityQueue<>(STRICT);
            for (int i = 0; i < n; i++) q.add(new Patient("P", s[i], i)); while (!q.isEmpty()) sink += q.poll().arrival; double a = (System.nanoTime() - t0) / 1e6;
            t0 = System.nanoTime(); LinkedList<Patient> ll = new LinkedList<>();
            for (int i = 0; i < n; i++) { Patient np = new Patient("P", s[i], i); ListIterator<Patient> it = ll.listIterator();
                while (it.hasNext()) { if (STRICT.compare(it.next(), np) > 0) { it.previous(); break; } } it.add(np); }
            while (!ll.isEmpty()) sink += ll.removeFirst().arrival; double b = (System.nanoTime() - t0) / 1e6;
            t0 = System.nanoTime(); @SuppressWarnings("unchecked") ArrayDeque<Patient>[] fifo = new ArrayDeque[5]; for (int i = 0; i < 5; i++) fifo[i] = new ArrayDeque<>();
            for (int i = 0; i < n; i++) fifo[s[i] - 1].add(new Patient("P", s[i], i));
            for (int served = 0; served < n; served++) for (ArrayDeque<Patient> d : fifo) if (!d.isEmpty()) { sink += d.poll().arrival; break; }
            double c = (System.nanoTime() - t0) / 1e6;
            System.out.printf("%-8d %16.2f %22.2f %20.2f%n", n, a, b, c);
        }
        if (sink == 1) System.out.print("");
    }
}
