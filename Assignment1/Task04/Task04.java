/*
 * TASK 4 - Classes, objects and constructors
 * Shows: default, parameterised and overloaded constructors, this, constructor chaining,
 * copy constructor, reference semantics (stack vs heap), and the two alternatives
 * (setters, static factory) with a timing comparison.
 */
public class Task04 {

    static class ParkingTicket {
        private final int ticketNo;
        private final String regNo;
        private final int floor, slot;
        private final int entryMinute;
        private final boolean monthlyPass;

        // default constructor: a blank "unassigned" ticket (chains to the full one)
        ParkingTicket() { this(0, "UNKNOWN", -1, -1, 0, false); }

        // overloaded: pass-less ticket (most common) -> chains
        ParkingTicket(int ticketNo, String regNo, int floor, int slot, int entryMinute) {
            this(ticketNo, regNo, floor, slot, entryMinute, false);
        }

        // master constructor: ALL validation and assignment lives here only
        ParkingTicket(int ticketNo, String regNo, int floor, int slot, int entryMinute, boolean monthlyPass) {
            this.ticketNo = ticketNo;
            this.regNo = regNo;
            this.floor = floor;
            this.slot = slot;
            this.entryMinute = entryMinute;
            this.monthlyPass = monthlyPass;
        }

        // copy constructor: re-issue a DUPLICATE ticket with the same number
        ParkingTicket(ParkingTicket other) {
            this(other.ticketNo, other.regNo, other.floor, other.slot, other.entryMinute, other.monthlyPass);
        }

        @Override public String toString() {
            return "Ticket#" + ticketNo + "[" + regNo + " F" + floor + "-S" + slot + " @" + entryMinute + (monthlyPass ? " PASS" : "") + "]";
        }
    }

    // ---- Alternative 1: default constructor + setters (object may be INVALID in between) ----
    static class SetterTicket {
        int ticketNo; String regNo; int floor, slot, entryMinute;
        void setTicketNo(int t) { ticketNo = t; }
        void setRegNo(String r) { regNo = r; }
        void setFloor(int f) { floor = f; }
        void setSlot(int s) { slot = s; }
        void setEntryMinute(int m) { entryMinute = m; }
    }

    // ---- Alternative 2: static factory method (named, can reject bad input, can cache) ----
    static class FactoryTicket {
        final int ticketNo; final String regNo; final int floor, slot, entryMinute;
        private FactoryTicket(int t, String r, int f, int s, int m) { ticketNo = t; regNo = r; floor = f; slot = s; entryMinute = m; }
        static FactoryTicket of(int t, String r, int f, int s, int m) {
            if (r == null || r.isEmpty()) throw new IllegalArgumentException("regNo required");
            return new FactoryTicket(t, r, f, s, m);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 4: Constructors ===");
        ParkingTicket blank = new ParkingTicket();
        ParkingTicket t1 = new ParkingTicket(1001, "TN01AB1111", 0, 0, 600);
        ParkingTicket t2 = new ParkingTicket(1002, "TN02CD2222", 0, 1, 605, true);
        ParkingTicket dup = new ParkingTicket(t1);               // copy constructor
        System.out.println("default   : " + blank);
        System.out.println("overloaded: " + t1);
        System.out.println("full      : " + t2);
        System.out.println("copy      : " + dup);
        System.out.println("t1 == dup (same object?)   : " + (t1 == dup));
        System.out.println("fields identical (toString): " + t1.toString().equals(dup.toString()));

        System.out.println("\nReferences: 't3 = t1' copies the REFERENCE (stack), not the object (heap):");
        ParkingTicket t3 = t1;
        System.out.println("t3 == t1 : " + (t3 == t1) + "   (one heap object, two reference variables)");

        System.out.println("\nSetter approach leaves an INVALID object between calls:");
        SetterTicket st = new SetterTicket();
        System.out.println("  after 'new' only: regNo=" + st.regNo + " ticketNo=" + st.ticketNo + " (not valid yet)");
        st.setRegNo("TN01AB1111"); st.setTicketNo(1001);
        System.out.println("  after setters   : regNo=" + st.regNo + " ticketNo=" + st.ticketNo);

        System.out.println("\nStatic factory rejects invalid input:");
        try { FactoryTicket.of(1, "", 0, 0, 0); } catch (IllegalArgumentException e) { System.out.println("  rejected: " + e.getMessage()); }
        System.out.println("  accepted: " + FactoryTicket.of(1001, "TN01AB1111", 0, 0, 600).regNo);

        // ---- timing for n objects ----
        int n = 5_000_000;
        ParkingTicket[] a = new ParkingTicket[n];
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) a[i] = new ParkingTicket(i, "TN01AB1111", 0, 0, 600);
        long tCons = System.nanoTime() - t0;
        SetterTicket[] b = new SetterTicket[n];
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) { SetterTicket s = new SetterTicket(); s.setTicketNo(i); s.setRegNo("TN01AB1111"); s.setFloor(0); s.setSlot(0); s.setEntryMinute(600); b[i] = s; }
        long tSet = System.nanoTime() - t0;
        FactoryTicket[] c = new FactoryTicket[n];
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) c[i] = FactoryTicket.of(i, "TN01AB1111", 0, 0, 600);
        long tFac = System.nanoTime() - t0;
        System.out.println("\nCreate n=" + n + " tickets (ms): constructor=" + tCons / 1_000_000
                + " setters=" + tSet / 1_000_000 + " factory=" + tFac / 1_000_000);
        System.out.println("All three are O(n) time and O(n) space (each ticket is O(1)); setters add n*5 extra method calls,");
        System.out.println("the factory adds one validation check. Only constructor/factory guarantee a valid object at creation.");
    }
}
