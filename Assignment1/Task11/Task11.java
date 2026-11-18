/*
 * TASK 11 - Complete implementation, testing and evaluation.
 * Uses: encapsulation (private fields), inheritance (Vehicle hierarchy), polymorphism
 * (calculateFee), abstraction (abstract Vehicle + Chargeable interface).
 */
import java.util.*;

interface Chargeable { double chargingFee(int kWh); }

abstract class Vehicle {
    private final String regNo;
    protected Vehicle(String regNo) { this.regNo = regNo; }
    public String getRegNo() { return regNo; }
    protected abstract double ratePerHour();
    protected abstract double dailyMax();
    public double calculateFee(int minutes) {
        if (minutes <= 0) return 0;
        int hours = (minutes + 59) / 60;                     // every started hour
        return Math.min(hours * ratePerHour(), dailyMax());
    }
    @Override public boolean equals(Object o) { return o instanceof Vehicle && regNo.equals(((Vehicle) o).regNo); }
    @Override public int hashCode() { return regNo.hashCode(); }
}
class TwoWheeler extends Vehicle {
    TwoWheeler(String r) { super(r); }
    protected double ratePerHour() { return 10; }
    protected double dailyMax() { return 50; }
}
class Car extends Vehicle {
    Car(String r) { super(r); }
    protected double ratePerHour() { return 30; }
    protected double dailyMax() { return 150; }
}
class ElectricCar extends Car implements Chargeable {
    ElectricCar(String r) { super(r); }
    public double chargingFee(int kWh) { return kWh * 12; }
}

class ParkingTicket {
    private static int counter = 1000;
    final int ticketNo; final Vehicle vehicle; final int floor, slot; final int entryMinute; final boolean pass;
    ParkingTicket(Vehicle v, int floor, int slot, int entryMinute, boolean pass) {
        this.ticketNo = ++counter; this.vehicle = v; this.floor = floor; this.slot = slot;
        this.entryMinute = entryMinute; this.pass = pass;
    }
}

class ParkingLot {
    private final ParkingTicket[][] slots;
    private final Map<String, ParkingTicket> active = new HashMap<>();
    private double collection = 0;

    ParkingLot(int[] perFloor) {
        slots = new ParkingTicket[perFloor.length][];
        for (int f = 0; f < perFloor.length; f++) slots[f] = new ParkingTicket[perFloor[f]];
    }
    static Vehicle create(String type, String reg) {
        switch (type) {
            case "BIKE": return new TwoWheeler(reg);
            case "CAR":  return new Car(reg);
            case "EV":   return new ElectricCar(reg);
            default: throw new IllegalArgumentException("Unknown vehicle type: " + type);
        }
    }
    ParkingTicket enter(String type, String reg, int minute, boolean pass) {
        Vehicle v = create(type, reg);
        if (active.containsKey(reg)) throw new IllegalStateException("Duplicate registration: " + reg);
        for (int f = 0; f < slots.length; f++)
            for (int s = 0; s < slots[f].length; s++)
                if (slots[f][s] == null) {
                    ParkingTicket t = new ParkingTicket(v, f, s, minute, pass);
                    slots[f][s] = t; active.put(reg, t);
                    return t;
                }
        return null;                                          // full
    }
    double exit(int floor, int slot, int minute, int kWh) {
        if (floor < 0 || floor >= slots.length || slot < 0 || slot >= slots[floor].length)
            throw new IllegalArgumentException("Invalid slot: (" + floor + "," + slot + ")");
        ParkingTicket t = slots[floor][slot];
        if (t == null) throw new IllegalStateException("Slot already free: (" + floor + "," + slot + ")");
        double fee = t.pass ? 0 : t.vehicle.calculateFee(minute - t.entryMinute);
        if (t.vehicle instanceof Chargeable c) fee += c.chargingFee(kWh);
        slots[floor][slot] = null; active.remove(t.vehicle.getRegNo());
        collection += fee;
        return fee;
    }
    double getCollection() { return collection; }
}

public class Task11 {
    static int pass = 0, fail = 0;
    static void check(String name, Object expected, Object actual) {
        boolean ok = Objects.equals(expected, actual);
        if (ok) pass++; else fail++;
        System.out.println(String.format("%-46s expected=%-10s actual=%-10s %s", name, expected, actual, ok ? "PASS" : "FAIL"));
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 11: Test table ===");
        ParkingLot lot = new ParkingLot(new int[]{2, 1});

        // ---- normal ----
        ParkingTicket a = lot.enter("BIKE", "TN01A1", 600, false);
        ParkingTicket b = lot.enter("CAR",  "TN02B2", 600, false);
        check("Normal: bike gets slot", "0-0", a.floor + "-" + a.slot);
        check("Normal: car gets next slot", "0-1", b.floor + "-" + b.slot);
        check("Normal: bike 130 min (3 h x 10)", 30.0, lot.exit(0, 0, 730, 0));

        // ---- boundary ----
        ParkingTicket c = lot.enter("EV", "TN03C3", 0, false);       // reuses slot 0-0
        check("Boundary: EV gets freed slot", "0-0", c.floor + "-" + c.slot);
        check("Boundary: last free slot used", "1-0", lot.enter("CAR", "TN04D4", 0, false).floor + "-0");
        check("Boundary: full facility returns null", null, lot.enter("CAR", "TN05E5", 0, false));
        check("Boundary: zero minutes -> Rs 0", 0.0, lot.exit(1, 0, 0, 0));
        check("Boundary: exactly 60 min car -> 1 h", 30.0, lot.exit(0, 1, 660, 0));
        ParkingTicket d = lot.enter("CAR", "TN06F6", 0, false);
        check("Boundary: 61 min car -> 2 h", 60.0, lot.exit(d.floor, d.slot, 61, 0));
        ParkingTicket e = lot.enter("CAR", "TN07G7", 0, false);
        check("Boundary: daily max (600 min car)", 150.0, lot.exit(e.floor, e.slot, 600, 0));
        ParkingTicket pp = lot.enter("CAR", "TN08H8", 0, true);
        check("Boundary: monthly pass free", 0.0, lot.exit(pp.floor, pp.slot, 300, 0));

        // ---- invalid ----
        try { lot.enter("TRUCK", "TN09J9", 0, false); check("Invalid: unknown type", "IAE", "none"); }
        catch (IllegalArgumentException ex) { check("Invalid: unknown type rejected", "IAE", "IAE"); }
        try { lot.enter("CAR", "TN03C3", 0, false); check("Invalid: duplicate regNo", "ISE", "none"); }
        catch (IllegalStateException ex) { check("Invalid: duplicate regNo rejected", "ISE", "ISE"); }
        try { lot.exit(9, 9, 10, 0); check("Invalid: bad slot", "IAE", "none"); }
        catch (IllegalArgumentException ex) { check("Invalid: bad slot rejected", "IAE", "IAE"); }
        try { lot.exit(0, 1, 10, 0); check("Invalid: free slot", "ISE", "none"); }
        catch (IllegalStateException ex) { check("Invalid: already-free slot rejected", "ISE", "ISE"); }

        // ---- day total, hand calculated ----
        System.out.println("\n=== Day scenario (fresh facility 2+1 slots) ===");
        ParkingLot day = new ParkingLot(new int[]{2, 1});
        ParkingTicket t1 = day.enter("BIKE", "B1", 540, false);   // 09:00
        ParkingTicket t2 = day.enter("CAR",  "C1", 540, false);   // 09:00
        ParkingTicket t3 = day.enter("EV",   "E1", 600, false);   // 10:00
        day.exit(t1.floor, t1.slot, 600, 0);     // 60 min  -> 1 h x 10        = 10
        day.exit(t2.floor, t2.slot, 700, 0);     // 160 min -> 3 h x 30        = 90
        day.exit(t3.floor, t3.slot, 725, 20);    // 125 min -> 3 h x 30 = 90 + 20 kWh x 12 = 240 -> 330
        System.out.println("Hand calculation: 10 + 90 + (90 + 240) = 430.0");
        System.out.println("Program output  : " + day.getCollection());
        check("Day total matches hand calculation", 430.0, day.getCollection());

        System.out.println("\nPASSED " + pass + "  FAILED " + fail);

        // ---- Alternative: finding an active ticket on exit ----
        System.out.println("\n=== Alternative storage: find ticket by regNo on exit ===");
        int n = 100_000;
        ParkingTicket[] arr = new ParkingTicket[n];
        ArrayList<ParkingTicket> list = new ArrayList<>();
        HashMap<String, ParkingTicket> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            ParkingTicket t = new ParkingTicket(new Car("TN" + i), 0, i, 0, false);
            arr[i] = t; list.add(t); map.put("TN" + i, t);
        }
        String key = "TN" + (n - 1);
        int R = 200;
        long t0 = System.nanoTime(); ParkingTicket f1 = null;
        for (int r = 0; r < R; r++) for (ParkingTicket t : arr) if (t.vehicle.getRegNo().equals(key)) { f1 = t; break; }
        long tA = (System.nanoTime() - t0) / R;
        t0 = System.nanoTime(); ParkingTicket f2 = null;
        for (int r = 0; r < R; r++) for (ParkingTicket t : list) if (t.vehicle.getRegNo().equals(key)) { f2 = t; break; }
        long tL = (System.nanoTime() - t0) / R;
        t0 = System.nanoTime(); ParkingTicket f3 = null;
        for (int r = 0; r < R * 1000; r++) f3 = map.get(key);
        long tM = (System.nanoTime() - t0) / (R * 1000L);
        System.out.println("n=" + n + " parked; find last vehicle: array=" + tA / 1000 + " us, ArrayList=" + tL / 1000 + " us, HashMap=" + tM + " ns");
        System.out.println("(found: " + (f1 == f2 && f2 == f3) + ")  Array/ArrayList O(n) time, O(n) space; HashMap O(1) average time, O(n) space");
        System.out.println("with extra overhead per entry (Node object + table slot, roughly 2-3x the memory of the array).");
    }
}
