/*
 * TASK 5 - static members and the object life cycle
 */
import java.util.*;

public class Task05 {

    static class Ticket {
        private static int counter;                 // shared by ALL tickets (class-level data)
        private static final List<String> LOG = new ArrayList<>();
        static { counter = 1000; LOG.add("static block ran once when class loaded"); }   // static block
        final int ticketNo;
        Ticket() { ticketNo = ++counter; }
        static int issued() { return counter - 1000; }          // static method
    }

    // ---- Alternative: counter as INSTANCE variable of the facility ----
    static class FacilityInstanceCounter {
        private int counter = 1000;
        int nextTicket() { return ++counter; }
    }
    // ---- Fix A: static counter shared by both gates (global unique numbers) ----
    static class FacilityStatic {
        private static int counter = 1000;
        int nextTicket() { return ++counter; }
    }
    // ---- Fix B: instance counter but with gate prefix (unique across gates) ----
    static class FacilityPrefixed {
        private final String gate; private int counter = 0;
        FacilityPrefixed(String gate) { this.gate = gate; }
        String nextTicket() { return gate + "-" + (++counter); }
    }

    static class Slot { boolean taken; }

    static void occupy(Slot s) { s.taken = true; }                // object passed by value of its reference
    static void reassign(Slot s) { s = new Slot(); s.taken = false; }   // reassigning local copy has no effect outside
    static Slot create() { return new Slot(); }                   // object returned

    public static void main(String[] args) throws Exception {
        System.out.println("=== TASK 5: static members & life cycle ===");
        System.out.println(Ticket.LOG.get(0));
        Ticket a = new Ticket(), b = new Ticket(), c = new Ticket();
        System.out.println("Ticket numbers: " + a.ticketNo + ", " + b.ticketNo + ", " + c.ticketNo + "  issued=" + Ticket.issued());

        System.out.println("\n-- Instance counter with two facilities (the problem) --");
        FacilityInstanceCounter north = new FacilityInstanceCounter(), south = new FacilityInstanceCounter();
        System.out.println("north: " + north.nextTicket() + "  south: " + south.nextTicket() + "   <-- SAME number 1001 issued twice!");
        System.out.println("-- Fix A: static counter --");
        FacilityStatic n2 = new FacilityStatic(), s2 = new FacilityStatic();
        System.out.println("north: " + n2.nextTicket() + "  south: " + s2.nextTicket() + "   unique globally");
        System.out.println("-- Fix B: instance counter + gate prefix --");
        FacilityPrefixed n3 = new FacilityPrefixed("N"), s3 = new FacilityPrefixed("S");
        System.out.println("north: " + n3.nextTicket() + "  south: " + s3.nextTicket() + "   unique, independent per gate");
        System.out.println("Space: static counter = 1 int total (O(1)); instance counter = k ints for k facilities (O(k)); tickets themselves O(n).");

        System.out.println("\n-- Passing / returning objects --");
        Slot s = create();
        occupy(s);
        System.out.println("after occupy(s): taken=" + s.taken + "   (method changed the caller's object)");
        reassign(s);
        System.out.println("after reassign(s): taken=" + s.taken + "   (reassigning the parameter does not change caller's reference)");

        System.out.println("\n-- Garbage collection --");
        Ticket temp = new Ticket();
        java.lang.ref.WeakReference<Ticket> ref = new java.lang.ref.WeakReference<>(temp);
        System.out.println("before leaving: ticket reachable = " + (ref.get() != null));
        temp = null;                                  // vehicle left: last strong reference dropped
        for (int i = 0; i < 5 && ref.get() != null; i++) { System.gc(); Thread.sleep(50); }
        System.out.println("after reference set to null + gc: ticket collected = " + (ref.get() == null));
    }
}
