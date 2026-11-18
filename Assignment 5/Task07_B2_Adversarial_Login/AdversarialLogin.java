import java.sql.*;
public class AdversarialLogin {
 static boolean unsafe(Connection c,String id,String pwd)throws SQLException{
  String sql="SELECT COUNT(*) FROM users WHERE id='"+id+"' AND pwd='"+pwd+"'";
  try(Statement st=c.createStatement();ResultSet rs=st.executeQuery(sql)){rs.next();return rs.getInt(1)>0;}
 }
 static boolean safe(Connection c,String id,String pwd)throws SQLException{
  String sql="SELECT COUNT(*) FROM users WHERE id=? AND pwd=?";
  try(PreparedStatement ps=c.prepareStatement(sql)){ps.setString(1,id);ps.setString(2,pwd);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1)>0;}}
 }
 public static void main(String[]args)throws Exception{
  String url=System.getenv().getOrDefault("DB_URL","jdbc:mysql://localhost:3306/assignment5");
  try(Connection c=DriverManager.getConnection(url,System.getenv().getOrDefault("DB_USER","root"),System.getenv().getOrDefault("DB_PASSWORD",""))){
   try(Statement s=c.createStatement()){s.executeUpdate("CREATE TABLE IF NOT EXISTS demo_users(id VARCHAR(80) PRIMARY KEY,pwd VARCHAR(80) NOT NULL)");s.executeUpdate("DELETE FROM demo_users");s.executeUpdate("INSERT INTO demo_users VALUES('student1','correct123')");}
   String attack="' OR '1'='1' -- ";
   System.out.println("Unsafe accepts crafted input: "+unsafe(c,attack,"wrong"));
   System.out.println("PreparedStatement accepts crafted input: "+safe(c,attack,"wrong"));
   System.out.println("Safe valid login: "+safe(c,"student1","correct123"));
  }
 }
}
