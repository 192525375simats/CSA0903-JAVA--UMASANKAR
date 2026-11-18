import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class FeeWindow extends JFrame {
    private final JTextField regNo = new JTextField(14);
    private final JTextField amount = new JTextField(10);
    private final JLabel status = new JLabel("Enter details and select Pay.");
    private final FeeService service = new FeeService();

    public FeeWindow() {
        super("Student Fee Payment");
        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Register number:")); form.add(regNo);
        form.add(new JLabel("Amount (INR):")); form.add(amount);
        JButton pay = new JButton("Pay");
        JButton clear = new JButton("Clear");
        JButton receipt = new JButton("Receipt");
        JPanel actions = new JPanel(new FlowLayout());
        actions.add(pay); actions.add(clear); actions.add(receipt);
        add(form, BorderLayout.CENTER); add(actions, BorderLayout.SOUTH); add(status, BorderLayout.NORTH);
        pay.addActionListener(e -> payFee());
        clear.addActionListener(e -> { regNo.setText(""); amount.setText(""); status.setText("Form cleared."); });
        receipt.addActionListener(e -> JOptionPane.showMessageDialog(this,
                service.getLastReceipt(), "Latest receipt", JOptionPane.INFORMATION_MESSAGE));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { dispose(); }
        });
        setSize(430, 190); setLocationRelativeTo(null);
    }
    private void payFee() {
        try {
            String id = regNo.getText().trim();
            BigDecimal value = new BigDecimal(amount.getText().trim());
            service.validate(id, value);
            status.setText("Processing payment...");
            setButtonsEnabled(false);
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() { return service.pay(id, value); }
                @Override protected void done() {
                    try { status.setText(get()); }
                    catch (Exception ex) { status.setText("Payment failed: " + ex.getMessage()); }
                    finally { setButtonsEnabled(true); }
                }
            }.execute();
        } catch (RuntimeException ex) {
            status.setText("Invalid input: " + ex.getMessage());
        }
    }
    private void setButtonsEnabled(boolean enabled) {
        for (Component c : ((JPanel)getContentPane().getComponent(1)).getComponents())
            c.setEnabled(enabled);
    }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new FeeWindow().setVisible(true)); }
}
class FeeService {
    private String lastReceipt = "No payment has been made.";
    void validate(String regNo, BigDecimal amount) {
        if (regNo.isBlank()) throw new IllegalArgumentException("Register number is required");
        if (!regNo.matches("[A-Za-z0-9]+")) throw new IllegalArgumentException("Invalid register number");
        if (amount.signum() <= 0 || amount.scale() > 2) throw new IllegalArgumentException("Enter a positive amount with at most 2 decimals");
    }
    String pay(String regNo, BigDecimal amount) {
        // Demo service: replace this stub with a server-side gateway/JDBC transaction.
        lastReceipt = "DEMO RECEIPT | " + regNo + " | INR " + amount + " | Payment simulated";
        return "Payment simulated successfully.";
    }
    String getLastReceipt() { return lastReceipt; }
}
