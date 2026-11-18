/*
 * TASK 2 - Choose the Data Representation
 *
 * slot number   : int      (small whole number, used as array index)
 * vehicle number: String   (letters + digits, e.g. TN01AB1111)
 * entry time    : int      (minutes since midnight; avoids floating point and time-zone issues)
 * duration      : int      (minutes)
 * fee           : double   (rupees, may contain paise after multiplying by rates)
 * floors/slots  : jagged array String[][] (each floor may have a different slot count)
 * input         : Scanner (here fed from a String so the program runs without typing)
 */
import java.util.*;

public class Task02 {
    // WRONG: integer division loses the partial hour
    static double feeWrong(int minutes, double rate) {
        int hours = minutes / 60;          // 130/60 = 2, the extra 10 minutes is lost
        return hours * rate;
    }
    // WRONG: casting AFTER the integer division does not recover the fraction
    static double feeWrongCast(int minutes, double rate) {
        double hours = (double) (minutes / 60);   // already truncated to 2
        return hours * rate;
    }
    // RIGHT: ceiling using integer arithmetic only (every started hour is charged)
    static double feeRight(int minutes, double rate) {
        int hours = (minutes + 59) / 60;
        return hours * rate;
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 2: Data types, arrays and casting ===");

        // ---- input with Scanner (simulated keyboard input) ----
        Scanner sc = new Scanner("TN01AB1111 130 30\n");
        String reg = sc.next();
        int minutes = sc.nextInt();
        double rate = sc.nextDouble();
        System.out.println("Read: reg=" + reg + " minutes=" + minutes + " rate=" + rate);

        // ---- casting risk ----
        System.out.println("\nCasting risk (130 minutes at Rs 30/hour, every started hour charged):");
        System.out.println("  feeWrong     = " + feeWrong(minutes, rate)     + "   <-- loses 10 minutes (customer undercharged)");
        System.out.println("  feeWrongCast = " + feeWrongCast(minutes, rate) + "   <-- cast after division does not help");
        System.out.println("  feeRight     = " + feeRight(minutes, rate)     + "   <-- ceiling with (m+59)/60");
        System.out.println("  Boundary 60 min -> " + feeRight(60, rate) + " (1 hour), 61 min -> " + feeRight(61, rate) + " (2 hours), 0 min -> " + feeRight(0, rate));
        long big = 3_000_000_000L;
        int narrowed = (int) big;
        System.out.println("  Narrowing long->int: " + big + " becomes " + narrowed + " (overflow, so never narrow money/time values)");

        // ---- jagged array ----
        int[] perFloor = {20, 50, 35, 60, 100};       // different slots on each floor
        String[][] slots = new String[perFloor.length][];
        for (int f = 0; f < perFloor.length; f++) slots[f] = new String[perFloor[f]];
        slots[1][49] = "TN09ZZ9999";
        System.out.println("\nJagged array: floors=" + slots.length);
        for (int f = 0; f < slots.length; f++) System.out.println("  floor " + f + " has " + slots[f].length + " slots");

        // ---- Alternative: flat 1-D array ----
        int maxPerFloor = 100;                         // flat index needs a FIXED slotsPerFloor
        String[] flat = new String[perFloor.length * maxPerFloor];
        flat[1 * maxPerFloor + 49] = "TN09ZZ9999";
        int used = 0; for (int p : perFloor) used += p;
        System.out.println("\nAlternative 1-D array (index = floor*" + maxPerFloor + " + slot):");
        System.out.println("  cells allocated = " + flat.length + ", cells really needed = " + used
                + " -> " + (flat.length - used) + " wasted cells");
        System.out.println("  locate slot: jagged = slots[f][s] O(1); flat = flat[f*W+s] O(1) (one multiply+add)");

        // ---- Alternative: ArrayList<String[]> ----
        ArrayList<String[]> list = new ArrayList<>();
        for (int p : perFloor) list.add(new String[p]);
        list.get(1)[49] = "TN09ZZ9999";
        System.out.println("\nAlternative ArrayList<String[]>: floors can be added at run time (list.add), jagged array cannot grow.");
        System.out.println("  locate slot: list.get(f)[s] O(1); extra space: ArrayList object + backing Object[] (O(F)).");

        // ---- timing locate ----
        int N = 20_000_000;
        long chk = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < N; i++) { int f = i % 5, s = i % 20; if (slots[f][s] == null) chk++; }
        long tJag = System.nanoTime() - t0;
        t0 = System.nanoTime();
        for (int i = 0; i < N; i++) { int f = i % 5, s = i % 20; if (flat[f * maxPerFloor + s] == null) chk++; }
        long tFlat = System.nanoTime() - t0;
        t0 = System.nanoTime();
        for (int i = 0; i < N; i++) { int f = i % 5, s = i % 20; if (list.get(f)[s] == null) chk++; }
        long tList = System.nanoTime() - t0;
        System.out.println("\nTime for " + N + " slot look-ups (ms): jagged=" + tJag / 1_000_000
                + " flat=" + tFlat / 1_000_000 + " arraylist=" + tList / 1_000_000 + "  (all O(1) each)  [chk=" + chk + "]");
    }
}
