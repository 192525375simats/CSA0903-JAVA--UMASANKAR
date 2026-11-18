import java.io.IOException;
import java.nio.file.*;

public class ATM {
    static final double BALANCE = 5000.0;
    static void withdraw(String accountText, String amountText) {
        try {
            int account = Integer.parseInt(accountText);
            double amount = Double.parseDouble(amountText);
            if (account <= 0) throw new IllegalArgumentException("Account must be positive");
            if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be positive");
            if (amount > BALANCE) throw new ArithmeticException("Insufficient balance");
            if (!Files.exists(Path.of("card-reader.txt"))) throw new IOException("Card-reader file is missing");
            System.out.printf("Account %d: Dispensed Rs. %.2f%n", account, amount);
        } catch (NumberFormatException e) {
            System.out.println("Input error: enter numeric account and amount");
        } catch (IllegalArgumentException | ArithmeticException e) {
            System.out.println("Transaction rejected: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Device error: " + e.getMessage());
        } finally {
            System.out.println("ATM session closed.");
        }
    }
    public static void main(String[] args) throws IOException {
        Files.writeString(Path.of("card-reader.txt"), "READY");
        System.out.println("TEST 1 - valid withdrawal"); withdraw("192525375", "1000");
        System.out.println("TEST 2 - letters"); withdraw("ABC", "100");
        System.out.println("TEST 3 - negative amount"); withdraw("192525375", "-50");
        System.out.println("TEST 4 - over balance"); withdraw("192525375", "9000");
        System.out.println("TEST 5 - missing card reader");
        Files.deleteIfExists(Path.of("card-reader.txt")); withdraw("192525375", "100");
    }
}
