import java.sql.*;
public class SafeLogin {
 public static boolean login(Connection c,String id,String password)throws SQLException{
  String sql="SELECT pwd_hash FROM users WHERE id=?";
  try(PreparedStatement ps=c.prepareStatement(sql)){
   ps.setString(1,id);
   try(ResultSet rs=ps.executeQuery()){
    if(!rs.next())return false;
    // Demonstration only: production must verify a salted adaptive hash (Argon2id/bcrypt),
    // not compare a plaintext password or store plaintext in the database.
    return PasswordDemo.verify(password,rs.getString(1));
   }
  }
 }
}
class PasswordDemo {
 static boolean verify(String supplied,String storedHash){
  // Placeholder to be replaced by a vetted password-hashing library integration.
  return storedHash!=null && storedHash.equals("DEMO_HASH:"+supplied);
 }
}
