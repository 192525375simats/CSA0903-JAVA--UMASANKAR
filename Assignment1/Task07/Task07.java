/*
 * TASK 7 - Override and specialise behaviour
 * @Override, final, and toString/equals/hashCode; alternative = switch on a type field.
 */
import java.util.*;

public class Task07 {

    static abstract class Vehicle {
        private final String regNo;                          // final variable: never changes after construction
        Vehicle(String regNo) { this.regNo = regNo; }
        String getRegNo() { return regNo; }
        protected abstract double ratePerHour();
        // final method: the billing formula must NOT be changed by subclasses
        public final double calculateFee(int minutes) { return ((minutes + 59) / 60) * ratePerHour(); }

        @Override public String toString() { return getClass().getSimpleName() + "(" + regNo + ")"; }
        @Override public boolean equals(Object o) { return o instanceof Vehicle && regNo.equals(((Vehicle) o).regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
    }
    static class TwoWheeler extends Vehicle {
        TwoWheeler(String r) { super(r); }
        @Override protected double ratePerHour() { return 10; }
    }
    static class Car extends Vehicle {
        Car(String r) { super(r); }
        @Override protected double ratePerHour() { return 30; }     // same signature, same return type, access not reduced
    }
    // final class: nobody may extend it
    static final class ElectricCar extends Car {
        ElectricCar(String r) { super(r); }
    }

    // ---- Alternative: one class + switch on a type field ----
    static class VehicleSwitch {
        final String regNo, type;
        VehicleSwitch(String r, String t) { regNo = r; type = t; }
        double calculateFee(int minutes) {
            int hours = (minutes + 59) / 60;
            switch (type) {
                case "BIKE": return hours * 10;
                case "CAR":  return hours * 30;
                case "EV":   return hours * 30;
                default: throw new IllegalArgumentException(type);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 7: Overriding ===");
        Vehicle[] vs = { new TwoWheeler("TN01A1"), new Car("TN02B2"), new ElectricCar("TN03C3") };
        for (Vehicle v : vs) System.out.println(v + " fee(130) = Rs " + v.calculateFee(130));

        System.out.println("\n-- equals / hashCode / toString --");
        Vehicle a = new Car("TN02B2"), b = new Car("TN02B2"), c = new Car("TN99X9");
        System.out.println("a.equals(b) (same regNo)      : " + a.equals(b));
        System.out.println("a.equals(c) (different regNo) : " + a.equals(c));
        System.out.println("a.hashCode()==b.hashCode()    : " + (a.hashCode() == b.hashCode()));
        Set<Vehicle> set = new HashSet<>(List.of(a, c));
        System.out.println("HashSet contains new Car(\"TN02B2\"): " + set.contains(b) + "   (works only because equals AND hashCode are overridden)");
        System.out.println("toString: " + a);

        System.out.println("\n-- final --");
        System.out.println("final field regNo: assignment after construction is a compile error");
        System.out.println("final method calculateFee(): subclasses cannot change the billing formula");
        System.out.println("final class ElectricCar: cannot be extended further");

        System.out.println("\n-- Alternative: switch on a type field --");
        VehicleSwitch[] sw = { new VehicleSwitch("TN01A1", "BIKE"), new VehicleSwitch("TN02B2", "CAR"), new VehicleSwitch("TN03C3", "EV") };
        for (VehicleSwitch v : sw) System.out.println(v.type + " " + v.regNo + " fee(130) = Rs " + v.calculateFee(130));
        System.out.println("Adding a new type: overriding = ADD 1 class, change 0 existing; switch = EDIT calculateFee() (and every other method with a switch).");

        int n = 20_000_000; double sum = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) sum += vs[i % 3].calculateFee(130);
        long t1 = System.nanoTime() - t0;
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) sum += sw[i % 3].calculateFee(130);
        long t2 = System.nanoTime() - t0;
        System.out.println(n + " fee calculations (ms): overriding=" + t1 / 1_000_000 + " switch=" + t2 / 1_000_000
                + "  override O(1); switch O(k) in worst case (String switch hashes, so close to O(1) here) [" + sum + "]");
    }
}
