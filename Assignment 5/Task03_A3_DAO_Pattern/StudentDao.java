import java.sql.*;
import java.util.*;

interface Dao<T,K> {
 K insert(T value) throws SQLException;
 Optional<T> findById(K id) throws SQLException;
 List<T> findAll() throws SQLException;
 boolean update(T value) throws SQLException;
 boolean delete(K id) throws SQLException;
}
record Student(int id,String registerNo,String name,String email) {}
public class StudentDao implements Dao<Student,Integer> {
 private final Connection connection;
 public StudentDao(Connection connection){this.connection=connection;}
 public Integer insert(Student s)throws SQLException{
  String sql="INSERT INTO student(register_no,name,email) VALUES(?,?,?)";
  try(PreparedStatement p=connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
   p.setString(1,s.registerNo());p.setString(2,s.name());p.setString(3,s.email());p.executeUpdate();
   try(ResultSet k=p.getGeneratedKeys()){if(k.next())return k.getInt(1);}
  }throw new SQLException("No generated key returned");
 }
 private Student map(ResultSet r)throws SQLException{return new Student(r.getInt("id"),r.getString("register_no"),r.getString("name"),r.getString("email"));}
 public Optional<Student> findById(Integer id)throws SQLException{
  try(PreparedStatement p=connection.prepareStatement("SELECT id,register_no,name,email FROM student WHERE id=?")){
   p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(map(r)):Optional.empty();}
  }
 }
 public List<Student> findAll()throws SQLException{
  List<Student> out=new ArrayList<>();try(PreparedStatement p=connection.prepareStatement("SELECT id,register_no,name,email FROM student ORDER BY id");ResultSet r=p.executeQuery()){while(r.next())out.add(map(r));}return out;
 }
 public boolean update(Student s)throws SQLException{
  try(PreparedStatement p=connection.prepareStatement("UPDATE student SET register_no=?,name=?,email=? WHERE id=?")){
   p.setString(1,s.registerNo());p.setString(2,s.name());p.setString(3,s.email());p.setInt(4,s.id());return p.executeUpdate()==1;
  }
 }
 public boolean delete(Integer id)throws SQLException{
  try(PreparedStatement p=connection.prepareStatement("DELETE FROM student WHERE id=?")){p.setInt(1,id);return p.executeUpdate()==1;}
 }
}
