/*
 * TASK 9 - Abstraction: abstract class + interfaces (default and static methods).
 */
import java.util.*;

interface Chargeable {
    double RATE_PER_KWH = 12;                                  // constant
    double chargingFee(int kWh);
    default String chargingSummary(int kWh) { return kWh + " kWh -> Rs " + chargingFee(kWh); }   // default method
    static boolean isValidKwh(int kWh) { return kWh >= 0 && kWh <= 100; }                         // static method
}

interface Payable {
    double amountDue(int minutes);
}

abstract class Vehicle {                                       // has STATE + constructor => abstract class
    private final String regNo;
    protected Vehicle(String regNo) { this.regNo = regNo; }
    String getRegNo() { return regNo; }
    protected abstract double ratePerHour();                   // abstract: each type MUST supply it
    double calculateFee(int minutes) { return ((minutes + 59) / 60) * ratePerHour(); }
}

class Car extends Vehicle implements Payable {
    Car(String r) { super(r); }
    protected double ratePerHour() { return 30; }
    public double amountDue(int m) { return calculateFee(m); }
}

// multiple behaviours: ElectricCar is-a Car AND can-do Chargeable AND Payable (via Car)
class ElectricCar extends Car implements Chargeable {
    ElectricCar(String r) { super(r); }
    public double chargingFee(int kWh) { return kWh * RATE_PER_KWH; }
}

// ---- Alternative: Vehicle as interface with defaults; Chargeable as abstract class ----
interface VehicleI {
    String getRegNo();                                         // no fields/constructors allowed, only behaviour
    double ratePerHour();
    default double calculateFee(int minutes) { return ((minutes + 59) / 60) * ratePerHour(); }
}
abstract class ChargeableA {
    abstract double chargingFee(int kWh);
}
class CarI implements VehicleI {
    private final String regNo;                                // state must live in the implementing class
    CarI(String r) { regNo = r; }                              // every implementer repeats this
    public String getRegNo() { return regNo; }
    public double ratePerHour() { return 30; }
}
// class ElectricCarI extends CarI, ChargeableA   <-- COMPILE ERROR: a class can extend only ONE class
class ElectricCarI extends ChargeableA implements VehicleI {   // so it can no longer extend CarI to reuse it
    public String getRegNo() { return "EV"; }
    public double ratePerHour() { return 30; }
    double chargingFee(int kWh) { return kWh * 12; }
}

public class Task09 {
    public static void main(String[] args) {
        System.out.println("=== TASK 9: Abstraction ===");
        // Vehicle v = new Vehicle("X");   // compile error: abstract class cannot be instantiated
        ElectricCar ev = new ElectricCar("TN03C3");
        System.out.println("fee(130)            = Rs " + ev.calculateFee(130));
        System.out.println("amountDue(130)      = Rs " + ev.amountDue(130) + "   (Payable)");
        System.out.println("chargingFee(10)     = Rs " + ev.chargingFee(10) + "   (Chargeable)");
        System.out.println("default method      : " + ev.chargingSummary(10));
        System.out.println("static method       : isValidKwh(150) = " + Chargeable.isValidKwh(150) + ", isValidKwh(40) = " + Chargeable.isValidKwh(40));
        System.out.println("ev is Vehicle/Payable/Chargeable: " + (ev instanceof Vehicle) + "/" + (ev instanceof Payable) + "/" + (ev instanceof Chargeable));

        System.out.println("\n-- Alternative design --");
        VehicleI c = new CarI("TN02B2");
        System.out.println("CarI fee(130) = " + c.calculateFee(130) + "  (works via default method)");
        System.out.println("Cannot be expressed any more: shared constructor, shared private regNo field (state),");
        System.out.println("and an EV that is BOTH a Car and Chargeable (class can extend only one class).");

        int n = 30_000_000; double sum = 0;
        Vehicle a = new Car("A"); VehicleI b = new CarI("B");
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) sum += a.calculateFee(130);
        long t1 = System.nanoTime() - t0;
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) sum += b.calculateFee(130);
        long t2 = System.nanoTime() - t0;
        System.out.println("\n" + n + " calls (ms): abstract class=" + t1 / 1_000_000 + " interface default=" + t2 / 1_000_000
                + " [" + sum + "]  both O(1); interface call needs an itable lookup (marginally slower, negligible).");
        System.out.println("Space per object: identical (object header + regNo reference).");
    }
}
