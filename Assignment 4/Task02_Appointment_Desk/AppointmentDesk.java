import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class AppointmentDesk extends JFrame {
    private final JTextField patientId = new JTextField(12);
    private final JComboBox<String> department = new JComboBox<>(new String[]{"Cardiology","Dermatology","Neurology"});
    private final JComboBox<String> doctor = new JComboBox<>();
    private final JComboBox<String> slot = new JComboBox<>(new String[]{"09:00","10:30","12:00","14:00","15:30"});
    private final JLabel status = new JLabel("Select department and appointment details.");
    private boolean bookingInProgress = false;
    private final Map<String, java.util.List<String>> doctors = Map.of(
        "Cardiology", java.util.List.of("Dr. Anand","Dr. Meera"),
        "Dermatology", java.util.List.of("Dr. Kavya","Dr. Joseph"),
        "Neurology", java.util.List.of("Dr. Raman","Dr. Priya"));

    public AppointmentDesk() {
        super("Hospital Outpatient Appointment Desk");
        JPanel form = new JPanel(new GridLayout(4,2,8,8));
        form.add(new JLabel("Patient ID:")); form.add(patientId);
        form.add(new JLabel("Department:")); form.add(department);
        form.add(new JLabel("Doctor:")); form.add(doctor);
        form.add(new JLabel("Time slot:")); form.add(slot);
        JButton book = new JButton("Book Appointment");
        JPanel bottom = new JPanel(new BorderLayout()); bottom.add(book,BorderLayout.NORTH); bottom.add(status,BorderLayout.SOUTH);
        add(form,BorderLayout.CENTER); add(bottom,BorderLayout.SOUTH);
        department.addActionListener(e -> refreshDoctors());
        book.addActionListener(e -> bookAppointment());
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (bookingInProgress) {
                    int choice=JOptionPane.showConfirmDialog(AppointmentDesk.this,
                        "A booking is in progress. Close anyway?","Confirm close",JOptionPane.YES_NO_OPTION);
                    if(choice==JOptionPane.YES_OPTION) dispose();
                } else dispose();
            }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        refreshDoctors(); setSize(470,260); setLocationRelativeTo(null);
    }
    private void refreshDoctors() {
        doctor.removeAllItems();
        for(String name:doctors.getOrDefault((String)department.getSelectedItem(),List.of()))
            doctor.addItem(name);
    }
    private void bookAppointment() {
        String id=patientId.getText().trim();
        if(id.isEmpty()){status.setText("Error: Patient ID is required.");return;}
        if(!id.matches("[A-Za-z0-9-]+")){status.setText("Error: Patient ID format is invalid.");return;}
        if(doctor.getSelectedItem()==null || slot.getSelectedItem()==null){status.setText("Error: Select doctor and slot.");return;}
        bookingInProgress=true;
        status.setText("Booking appointment...");
        Timer timer=new Timer(900,e->{
            bookingInProgress=false;
            status.setText("Confirmed: "+id+" / "+doctor.getSelectedItem()+" / "+slot.getSelectedItem());
            JOptionPane.showMessageDialog(this,status.getText(),"Appointment confirmed",JOptionPane.INFORMATION_MESSAGE);
        });
        timer.setRepeats(false); timer.start();
    }
    public static void main(String[] args){SwingUtilities.invokeLater(()->new AppointmentDesk().setVisible(true));}
}
