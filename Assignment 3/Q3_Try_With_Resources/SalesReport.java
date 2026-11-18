import java.io.*;
import java.nio.file.*;
import java.util.*;
public class SalesReport {
    static void manual(List<String> names) {
        System.out.println("Manual close version:");
        for (String name : names) {
            BufferedReader reader = null;
            try {
                reader = Files.newBufferedReader(Path.of(name));
                System.out.println(reader.readLine());
            } catch (IOException e) {
                System.out.println("Failed at " + name + ": " + e.getMessage());
            } finally {
                if (reader != null) try { reader.close(); } catch (IOException e) { System.out.println("Close failed"); }
            }
        }
    }
    static void automatic(List<String> names) {
        System.out.println("Try-with-resources version:");
        for (String name : names) {
            try (BufferedReader reader = Files.newBufferedReader(Path.of(name))) {
                System.out.println(reader.readLine());
            } catch (IOException e) {
                System.out.println("Failed at " + name + ": " + e.getMessage());
            }
        }
    }
    public static void main(String[] args) throws IOException {
        Files.writeString(Path.of("store1.txt"), "Store 1: 1200");
        Files.writeString(Path.of("store3.txt"), "Store 3: 700");
        List<String> inputs = List.of("store1.txt", "missing-store.txt", "store3.txt");
        manual(inputs);
        automatic(inputs);
        System.out.println("Both versions continue and close opened readers.");
    }
}
