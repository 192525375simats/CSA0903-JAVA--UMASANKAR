package parking.app;

import parking.model.Booking;
import parking.service.BookingService;
import java.util.*;

public class Task10 {

    // ---- Alternative: public fields, validation left to the caller ----
    static class OpenBooking { public String regNo; public int durationMinutes; public int slotNumber; }

    public static void main(String[] args) {
        System.out.println("=== TASK 10: Packages & encapsulation ===");
        System.out.println("Packages: parking.model (Booking), parking.service (BookingService), parking.app (Task10)");

        BookingService svc = new BookingService();
        Booking b = new Booking("TN01AB1111", 130, 7);
        System.out.println("Valid booking fee(130 min) = Rs " + svc.fee(b));

        System.out.println("\nValidating setters:");
        try { b.setDurationMinutes(-5); } catch (IllegalArgumentException e) { System.out.println("  rejected: " + e.getMessage()); }
        try { b.setSlotNumber(500); }     catch (IllegalArgumentException e) { System.out.println("  rejected: " + e.getMessage()); }
        try { b.setRegNo("bad"); }        catch (IllegalArgumentException e) { System.out.println("  rejected: " + e.getMessage()); }
        System.out.println("  state unchanged after rejections: duration=" + b.getDurationMinutes() + " slot=" + b.getSlotNumber() + " reg=" + b.getRegNo());
        // b.regNo = "x";            // compile error: regNo has private access in Booking
        // b.packageVisible = 2;     // compile error: not public, different package
        // b.rate = 1;               // compile error: protected, accessed from non-subclass in another package

        System.out.println("\nWrapper classes and autoboxing:");
        Integer x = 127, y = 127, p = 1000, q = 1000;
        System.out.println("  Integer 127 == 127   : " + (x == y) + "   (cached -128..127)");
        System.out.println("  Integer 1000 == 1000 : " + (p == q) + "  <-- compares references, NOT values (risk!)");
        System.out.println("  1000 equals 1000     : " + p.equals(q) + "   (always use equals / intValue)");
        Map<String, Integer> slotOf = new HashMap<>();
        System.out.println("  unboxing null risk   : ");
        try { int s = slotOf.get("TN01AB1111"); } catch (NullPointerException e) { System.out.println("    NullPointerException when auto-unboxing a missing map value"); }

        System.out.println("\n-- Alternative: public fields --");
        OpenBooking ob = new OpenBooking();
        ob.regNo = "???"; ob.durationMinutes = -999; ob.slotNumber = 12345;      // nothing stops this
        System.out.println("  invalid state accepted silently: " + ob.regNo + ", " + ob.durationMinutes + ", " + ob.slotNumber);
        System.out.println("  every caller must repeat the same 3 checks: validation code duplicated in N places instead of 1.");

        int n = 20_000_000; long ok = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) { b.setDurationMinutes(i % 1000); ok++; }
        long t1 = System.nanoTime() - t0;
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) { int d = i % 1000; if (d >= 0) { ob.durationMinutes = d; ok++; } }
        long t2 = System.nanoTime() - t0;
        System.out.println("  " + n + " updates (ms): validating setter=" + t1 / 1_000_000 + " public field + caller check=" + t2 / 1_000_000
                + "  [" + ok + "]  both O(1) per update; the check cost is the same, only its location differs.");
    }
}
