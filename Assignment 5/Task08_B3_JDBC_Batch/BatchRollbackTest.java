import java.sql.*;
import java.util.*;
public class BatchRollbackTest {
 public static void main(String[]a)throws Exception{
  String url=System.getenv().getOrDefault("DB_URL","jdbc:mysql://localhost:3306/assignment5");
  try(Connection c=DriverManager.getConnection(url,System.getenv().getOrDefault("DB_USER","root"),System.getenv().getOrDefault("DB_PASSWORD",""))){
   try(Statement s=c.createStatement()){s.executeUpdate("DELETE FROM bus_pass");}
   List<String[]> rows=new ArrayList<>();rows.add(new String[]{"P1","R1","Asha","1","100.00"});rows.add(new String[]{"P2","R2","Bad","2","not-a-number"});rows.add(new String[]{"P3","R3","Kiran","3","300.00"});
   try{PassLoader.loadAll(c,rows);}catch(NumberFormatException ex){System.out.println("Invalid amount rejected: "+ex.getMessage());}
   try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM bus_pass")){r.next();System.out.println("Rows remaining after rollback: "+r.getInt(1));}
  }
 }
}
