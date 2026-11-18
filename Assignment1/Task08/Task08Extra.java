/*
 * TASK 8 - Polymorphism: overloading (compile time), dynamic dispatch (run time),
 * upcasting into one array, and the instanceof/cast alternative.
 */
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Task08Extra {

    static abstract class Veh {
        final String regNo;
        Veh(String r) { regNo = r; }
        abstract double rate();
        // overload 1: minutes
        double calculateFee(int minutes) { return ((minutes + 59) / 60) * rate(); }
        // overload 2: entry/exit time (compiler picks by argument types)
        double calculateFee(LocalTime in, LocalTime out) {
            return calculateFee((int) ChronoUnit.MINUTES.between(in, out));
        }
    }
    static class Bike extends Veh { Bike(String r) { super(r); } double rate() { return 10; } }
    static class Car extends Veh  { Car(String r)  { super(r); } double rate() { return 30; } }
    static class Ev extends Veh   { Ev(String r)   { super(r); } double rate() { return 30; }
        @Override double calculateFee(int m) { return super.calculateFee(m) + 5; } }   // extra Rs 5 EV surcharge

    // ---- Alternative: instanceof + cast ----
    static double feeInstanceof(Object v, int minutes) {
        int h = (minutes + 59) / 60;
        if (v instanceof Ev) return h * 30 + 5;
        else if (v instanceof Car) return h * 30;
        else if (v instanceof Bike) return h * 10;
        throw new IllegalArgumentException("unknown");
    }

    public static void main(String[] args) {
        System.out.println("=== TASK 8: Polymorphism ===");
        Veh[] parked = { new Bike("TN01A1"), new Car("TN02B2"), new Ev("TN03C3") };   // upcasting
        for (Veh v : parked)
            System.out.println(v.getClass().getSimpleName() + " " + v.regNo + " fee(130) = " + v.calculateFee(130));

        System.out.println("\nCompile-time polymorphism (overloading):");
        Veh car = new Car("TN02B2");
        System.out.println("  calculateFee(int)             -> " + car.calculateFee(130));
        System.out.println("  calculateFee(LocalTime,LocalTime) 10:00->12:10 -> " + car.calculateFee(LocalTime.of(10, 0), LocalTime.of(12, 10)));

        System.out.println("\nDynamic dispatch trace for  Veh v = new Ev(\"TN03C3\"); v.calculateFee(130);");
        Veh v = new Ev("TN03C3");
        System.out.println("  declared (reference) type : Veh");
        System.out.println("  actual (object) type      : " + v.getClass().getSimpleName());
        System.out.println("  method executed           : Ev.calculateFee(int) -> super.calculateFee -> Ev.rate()  = " + v.calculateFee(130));
        System.out.println("  (the JVM looks at the ACTUAL type at run time, not the declared type)");

        System.out.println("\nAlternative: instanceof + casting gives same answers:");
        for (Veh x : parked) System.out.println("  " + x.getClass().getSimpleName() + " -> " + feeInstanceof(x, 130));

        int n = 30_000_000; double sum = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) sum += parked[i % 3].calculateFee(130);
        long t1 = System.nanoTime() - t0;
        t0 = System.nanoTime();
        for (int i = 0; i < n; i++) sum += feeInstanceof(parked[i % 3], 130);
        long t2 = System.nanoTime() - t0;
        System.out.println("\n" + n + " calls (ms): dynamic dispatch=" + t1 / 1_000_000 + " instanceof chain=" + t2 / 1_000_000 + " [" + sum + "]");
        System.out.println("Dispatch O(1) via vtable; instanceof chain O(k) for k types. New type: 1 new class vs edit every chain.");
        System.out.println("Note: with only k=3 types the JIT optimises the short instanceof chain well, so it measured faster here;");
        System.out.println("the chain's cost grows with k (and with every new type edited in), dispatch stays constant, and only dispatch is open for extension.");
    }
}
