import java.sql.*;
import java.util.*;
public class PassLoaderBenchmark {
 static List<String[]> data(int n){List<String[]> x=new ArrayList<>(n);for(int i=1;i<=n;i++)x.add(new String[]{"P"+i,"R"+i,"Student"+i,String.valueOf(i%20+1),"4500.00"});return x;}
 static void clear(Connection c)throws SQLException{try(Statement s=c.createStatement()){s.executeUpdate("DELETE FROM bus_pass");}}
 static long statement(Connection c,List<String[]> rows,boolean tx)throws SQLException{
  clear(c);c.setAutoCommit(!tx);long t=System.nanoTime();
  try(Statement s=c.createStatement()){for(String[]p:rows){String sql="INSERT INTO bus_pass(pass_id,reg_no,name,route_no,amount) VALUES('"+p[0]+"','"+p[1]+"','"+p[2]+"',"+p[3]+","+p[4]+")";s.executeUpdate(sql);}if(tx)c.commit();}
  catch(SQLException e){if(tx)c.rollback();throw e;}finally{c.setAutoCommit(true);}return System.nanoTime()-t;
 }
 static long prepared(Connection c,List<String[]> rows)throws SQLException{
  clear(c);String sql="INSERT INTO bus_pass(pass_id,reg_no,name,route_no,amount) VALUES(?,?,?,?,?)";long t=System.nanoTime();
  try(PreparedStatement p=c.prepareStatement(sql)){for(String[]x:rows){p.setString(1,x[0]);p.setString(2,x[1]);p.setString(3,x[2]);p.setInt(4,Integer.parseInt(x[3]));p.setDouble(5,Double.parseDouble(x[4]));p.executeUpdate();}}return System.nanoTime()-t;
 }
 static long batch(Connection c,List<String[]> rows)throws SQLException{clear(c);long t=System.nanoTime();PassLoader.loadAll(c,rows);return System.nanoTime()-t;}
 public static void main(String[]a)throws Exception{
  String url=System.getenv().getOrDefault("DB_URL","jdbc:mysql://localhost:3306/assignment5");
  try(Connection c=DriverManager.getConnection(url,System.getenv().getOrDefault("DB_USER","root"),System.getenv().getOrDefault("DB_PASSWORD",""))){
   for(int n:new int[]{1000,10000,100000}){List<String[]> rows=data(n);
    System.out.printf("%d rows | Statement %.2f ms | Prepared %.2f ms | Batch+transaction %.2f ms%n",n,statement(c,rows,false)/1e6,prepared(c,rows)/1e6,batch(c,rows)/1e6);
   }
  }
 }
}
