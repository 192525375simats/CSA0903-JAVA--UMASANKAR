/*
 * TASK 3 - Control logic: fee slabs (if-else / switch, break/continue/return)
 * and the alternative allocator (free-count per floor + stack of free slots).
 */
import java.util.*;

public class Task03Extra {

    // ---------- Fee computation: slabs with if-else ----------
    // first hour flat, each additional started hour cheaper, daily maximum
    static double fee(String type, int minutes) {
        if (minutes <= 0) return 0;                       // return early: nothing to compute
        double first, extra, dailyMax;
        switch (type) {                                    // switch on vehicle type
            case "BIKE": first = 10; extra = 5;  dailyMax = 50;  break;
            case "CAR":  first = 30; extra = 20; dailyMax = 150; break;
            case "EV":   first = 30; extra = 20; dailyMax = 150; break;
            default: throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }
        int hours = (minutes + 59) / 60;                  // every started hour
        double f = first + (hours - 1) * extra;
        return Math.min(f, dailyMax);                      // daily maximum
    }

    // ---------- Alternative allocator: free count per floor + stack of free slots ----------
    static class FastFacility {
        final String[][] slots;
        final int[] freeCount;
        final Deque<Integer>[] freeStack;
        @SuppressWarnings("unchecked")
        FastFacility(int[] perFloor) {
            slots = new String[perFloor.length][];
            freeCount = new int[perFloor.length];
            freeStack = new Deque[perFloor.length];
            for (int f = 0; f < perFloor.length; f++) {
                slots[f] = new String[perFloor[f]];
                freeCount[f] = perFloor[f];
                freeStack[f] = new ArrayDeque<>();
                for (int s = perFloor[f] - 1; s >= 0; s--) freeStack[f].push(s);   // lowest slot on top
            }
        }
        int[] allocate(String v) {
            for (int f = 0; f < slots.length; f++) {
                if (freeCount[f] == 0) continue;           // skip full floor in O(1)
                int s = freeStack[f].pop();                 // O(1)
                freeCount[f]--;
                slots[f][s] = v;
                return new int[]{f, s};
            }
            return null;
        }
        boolean release(int f, int s) {
            if (f < 0 || f >= slots.length || s < 0 || s >= slots[f].length || slots[f][s] == null) return false;
            slots[f][s] = null;
            freeStack[f].push(s);                           // O(1)
            freeCount[f]++;
            return true;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 3 extra: fee slabs ===");
        int[] tests = {0, 1, 60, 61, 120, 130, 600, 1440};
        for (int m : tests)
            System.out.println(String.format("CAR %4d min -> Rs %6.1f | BIKE -> Rs %5.1f", m, fee("CAR", m), fee("BIKE", m)));
        try { fee("TRUCK", 30); } catch (IllegalArgumentException e) { System.out.println("Unknown type handled: " + e.getMessage()); }

        System.out.println("\n=== Alternative allocator gives the same answers ===");
        FastFacility p = new FastFacility(new int[]{2, 1});
        System.out.println(Arrays.toString(p.allocate("TN01AB1111")));
        System.out.println(Arrays.toString(p.allocate("TN02CD2222")));
        System.out.println(Arrays.toString(p.allocate("TN03EF3333")));
        System.out.println(Arrays.toString(p.allocate("TN04GH4444")));
        System.out.println(p.release(0, 1) + " " + p.release(0, 1) + " " + p.release(5, 0));
        System.out.println(Arrays.toString(p.allocate("TN04GH4444")));

        // ---------- measurement: nearly-full facility, 5 floors x 100 slots ----------
        int[] big = {100, 100, 100, 100, 100};
        ParkingFacility slow = new ParkingFacility(big);
        FastFacility fast = new FastFacility(big);
        for (int i = 0; i < 499; i++) { slow.allocate("V" + i); fast.allocate("V" + i); }
        int R = 200_000;
        long t0 = System.nanoTime();
        for (int i = 0; i < R; i++) { slow.allocate("X"); slow.release(4, 99); }
        long tSlow = System.nanoTime() - t0;
        t0 = System.nanoTime();
        for (int i = 0; i < R; i++) { fast.allocate("X"); fast.release(4, 99); }
        long tFast = System.nanoTime() - t0;
        System.out.println("\n" + R + " allocate+release on a nearly full 5x100 facility:");
        System.out.println("  linear scan  : " + tSlow / 1_000_000 + " ms   (allocate O(F*S), release O(1), extra space O(1))");
        System.out.println("  count+stack  : " + tFast / 1_000_000 + " ms   (allocate O(F) worst case, release O(1), extra space O(F*S))");
    }
}
