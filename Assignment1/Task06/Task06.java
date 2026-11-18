/*
 * TASK 6 - Inheritance hierarchy
 *
 *            Vehicle (abstract)                    Hierarchical: TwoWheeler, Car extend Vehicle
 *            /            \                        Multilevel   : ElectricCar -> Car -> Vehicle
 *       TwoWheeler        Car
 *                          |
 *                      ElectricCar
 *
 * UML (text form):
 *   +--------------------------+
 *   | Vehicle  <<abstract>>    |
 *   | - regNo : String         |
 *   | + getRegNo()             |
 *   | # ratePerHour() : double |
 *   | + calculateFee(int)      |
 *   +--------------------------+
 *        ^                ^
 *   +------------+   +------------+
 *   | TwoWheeler |   |    Car     |
 *   +------------+   +------------+
 *                          ^
 *                    +--------------+
 *                    | ElectricCar  |
 *                    | + chargeRate |
 *                    +--------------+
 */
public class Task06 {

    static abstract class Vehicle {
        private final String regNo;
        Vehicle(String regNo) { System.out.println("  1. Vehicle constructor  (regNo=" + regNo + ")"); this.regNo = regNo; }
        String getRegNo() { return regNo; }
        protected abstract double ratePerHour();
        double calculateFee(int minutes) { return ((minutes + 59) / 60) * ratePerHour(); }
    }
    static class TwoWheeler extends Vehicle {
        TwoWheeler(String r) { super(r); System.out.println("  2. TwoWheeler constructor"); }
        @Override protected double ratePerHour() { return 10; }
    }
    static class Car extends Vehicle {
        Car(String r) { super(r); System.out.println("  2. Car constructor"); }
        @Override protected double ratePerHour() { return 30; }
        String describe() { return "Car " + getRegNo(); }
    }
    static class ElectricCar extends Car {
        final double chargeRatePerKwh = 12;
        ElectricCar(String r) { super(r); System.out.println("  3. ElectricCar constructor (adds chargeRate=" + chargeRatePerKwh + ")"); }
        @Override String describe() { return "Electric " + super.describe(); }       // super for a method
        @Override protected double ratePerHour() { return super.ratePerHour(); }     // reuse Car's rate
    }

    // ---- Alternative: composition ----
    static class VehicleType {
        final String name; final double rate;
        VehicleType(String n, double r) { name = n; rate = r; }
    }
    static class VehicleC {                      // ONE class, behaviour injected
        final String regNo; final VehicleType type;
        VehicleC(String r, VehicleType t) { regNo = r; type = t; }
        double calculateFee(int minutes) { return ((minutes + 59) / 60) * type.rate; }
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 6: Inheritance ===");
        System.out.println("Constructor trace for: new ElectricCar(\"TN03C3\")");
        ElectricCar ev = new ElectricCar("TN03C3");
        System.out.println("describe() -> " + ev.describe());
        System.out.println("fee(130 min) -> " + ev.calculateFee(130));
        System.out.println("ev is Car? " + (ev instanceof Car) + "  ev is Vehicle? " + (ev instanceof Vehicle));

        System.out.println("\nWhat is inherited: regNo (via getter), calculateFee(); what is added: chargeRatePerKwh, describe() override.");

        System.out.println("\n-- Alternative: composition --");
        VehicleType bike = new VehicleType("BIKE", 10), car = new VehicleType("CAR", 30), ebike = new VehicleType("EBIKE", 8);
        VehicleC[] vs = { new VehicleC("TN01A1", bike), new VehicleC("TN02B2", car), new VehicleC("TN05E5", ebike) };
        for (VehicleC v : vs) System.out.println(v.type.name + " " + v.regNo + " fee(130)=" + v.calculateFee(130));
        System.out.println("New type with composition: just 'new VehicleType(...)' (data), ZERO new classes.");
        System.out.println("New type with inheritance: write one new subclass (1 new class, nothing else changes).");

        int n = 10_000_000; double sum = 0;
        Vehicle inh = new Car("TN02B2"); VehicleC comp = new VehicleC("TN02B2", car);
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) sum += inh.calculateFee(130);
        long t1 = System.nanoTime() - t0;
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) sum += comp.calculateFee(130);
        long t2 = System.nanoTime() - t0;
        System.out.println("\n" + n + " fee calculations (ms): inheritance=" + t1 / 1_000_000 + " composition=" + t2 / 1_000_000 + " (both O(1)) [" + sum + "]");
        System.out.println("Space per object: inheritance = regNo ref; composition = regNo ref + type ref (one extra reference, shared VehicleType objects).");
    }
}
