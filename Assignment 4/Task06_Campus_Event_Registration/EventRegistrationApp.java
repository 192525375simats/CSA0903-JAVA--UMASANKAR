import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EventRegistrationApp extends JFrame {
    private final JTextField studentId=new JTextField(10), studentName=new JTextField(14);
    private final JComboBox<String> event=new JComboBox<>(new String[]{"Cultural Night","Tech Fest","Sports Day"});
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"ID","Student","Event","Registered At"},0);
    private final JTable table=new JTable(model);
    private final JLabel status=new JLabel("Ready");
    private final RegistrationService service=new RegistrationService();

    public EventRegistrationApp(){
        super("Campus Event Registration");
        JPanel form=new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Student ID"));form.add(studentId);form.add(new JLabel("Name"));form.add(studentName);form.add(event);
        JButton register=new JButton("Register"),refresh=new JButton("Refresh"),export=new JButton("Export CSV");
        form.add(register);form.add(refresh);form.add(export);
        JMenuBar bar=new JMenuBar();JMenu file=new JMenu("File");JMenuItem reload=new JMenuItem("Reload"),exit=new JMenuItem("Exit");file.add(reload);file.add(exit);bar.add(file);setJMenuBar(bar);
        add(form,BorderLayout.NORTH);add(new JScrollPane(table),BorderLayout.CENTER);add(status,BorderLayout.SOUTH);
        register.addActionListener(e->register());refresh.addActionListener(e->loadPage(0));
        reload.addActionListener(e->loadPage(0));exit.addActionListener(e->dispose());export.addActionListener(e->exportCsv());
        setSize(900,500);setLocationRelativeTo(null);loadPage(0);
    }
    private void register(){
        String id=studentId.getText().trim(),name=studentName.getText().trim(),eventName=(String)event.getSelectedItem();
        if(id.isEmpty()||name.isEmpty()){status.setText("Enter student ID and name.");return;}
        if(!id.matches("[A-Za-z0-9-]+")){status.setText("Invalid student ID.");return;}
        setBusy(true);
        new SwingWorker<String,Void>(){
            protected String doInBackground() throws Exception{return service.register(id,name,eventName);}
            protected void done(){try{status.setText(get());loadPage(0);}catch(Exception ex){status.setText(rootMessage(ex));}finally{setBusy(false);}}
        }.execute();
    }
    private void loadPage(int offset){
        setBusy(true);
        new SwingWorker<List<String[]>,Void>(){
            protected List<String[]> doInBackground() throws Exception{return service.page(50,offset);}
            protected void done(){try{model.setRowCount(0);for(String[] row:get())model.addRow(row);status.setText("Loaded page (max 50 rows).");}catch(Exception ex){status.setText(rootMessage(ex));}finally{setBusy(false);}}
        }.execute();
    }
    private void exportCsv(){
        JFileChooser chooser=new JFileChooser();if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        try(PrintWriter out=new PrintWriter(chooser.getSelectedFile())){
            for(int c=0;c<model.getColumnCount();c++){if(c>0)out.print(",");out.print(model.getColumnName(c));}out.println();
            for(int r=0;r<model.getRowCount();r++){for(int c=0;c<model.getColumnCount();c++){if(c>0)out.print(",");out.print('"'+String.valueOf(model.getValueAt(r,c)).replace("\"","\\"")+'"');}out.println();}
            status.setText("CSV exported.");
        }catch(IOException ex){status.setText("Export failed: "+ex.getMessage());}
    }
    private void setBusy(boolean busy){studentId.setEnabled(!busy);studentName.setEnabled(!busy);}
    private String rootMessage(Exception e){Throwable t=e;while(t.getCause()!=null)t=t.getCause();return "Operation failed: "+t.getMessage();}
    public static void main(String[] args){SwingUtilities.invokeLater(()->new EventRegistrationApp().setVisible(true));}
}
class RegistrationService {
    private Connection connection() throws SQLException {
        String url=System.getenv().getOrDefault("DB_URL","jdbc:mysql://localhost:3306/campus_events");
        String user=System.getenv().getOrDefault("DB_USER","root");
        String password=System.getenv().getOrDefault("DB_PASSWORD","");
        return DriverManager.getConnection(url,user,password);
    }
    String register(String id,String name,String event) throws SQLException {
        String sql="INSERT INTO registrations(student_id,student_name,event_name) VALUES(?,?,?)";
        try(Connection c=connection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,id);p.setString(2,name);p.setString(3,event);p.executeUpdate();
            return "Registration successful.";
        }catch(SQLIntegrityConstraintViolationException ex){return "Duplicate registration: this student is already registered for this event.";}
    }
    List<String[]> page(int limit,int offset) throws SQLException {
        String sql="SELECT student_id,student_name,event_name,registered_at FROM registrations ORDER BY registered_at DESC LIMIT ? OFFSET ?";
        List<String[]> rows=new ArrayList<>();
        try(Connection c=connection();PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,limit);p.setInt(2,offset);
            try(ResultSet rs=p.executeQuery()){while(rs.next())rows.add(new String[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)});}
        }return rows;
    }
}
