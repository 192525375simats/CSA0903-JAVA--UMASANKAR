import java.awt.*;import java.awt.event.*;import javax.swing.*;
public class ConverterFrameListener extends JFrame implements ActionListener {
 private final JTextField amount=new JTextField(10); private final JComboBox<String> target=new JComboBox<>(new String[]{"USD","EUR"}); private final JLabel result=new JLabel(" ");
 public ConverterFrameListener(){super("Converter - Frame Listener");setLayout(new FlowLayout());JButton b=new JButton("Convert");add(amount);add(target);add(b);add(result);b.addActionListener(this);amount.addActionListener(this);addWindowListener(new WindowAdapter(){@Override public void windowClosing(WindowEvent e){dispose();}});setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);pack();}
 public void actionPerformed(ActionEvent e){try{double v=Double.parseDouble(amount.getText().trim());if(v<0||!Double.isFinite(v))throw new NumberFormatException();String c=(String)target.getSelectedItem();String s=String.format("%.2f %s",v*(c.equals("USD")?.012:.011),c);result.setText(s);JOptionPane.showMessageDialog(this,s);}catch(NumberFormatException ex){result.setText("Invalid amount");JOptionPane.showMessageDialog(this,"Invalid amount","Error",JOptionPane.ERROR_MESSAGE);}}
 public static void main(String[]x){SwingUtilities.invokeLater(()->new ConverterFrameListener().setVisible(true));}
}
