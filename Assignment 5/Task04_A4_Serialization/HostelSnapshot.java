import java.io.*;
import java.util.*;

class Hostel implements Serializable {
 private static final long serialVersionUID=1L;
 String hostelName;
 List<Room> rooms=new ArrayList<>();
}
class Room implements Serializable {
 private static final long serialVersionUID=1L;
 int roomNo;
 List<Resident> residents=new ArrayList<>();
}
class Resident implements Serializable {
 private static final long serialVersionUID=1L;
 String name,registerNo,phoneNumber;
 transient String password;
 transient java.sql.Connection liveConnection;
 Resident(String n,String id,String phone,String pwd){name=n;registerNo=id;phoneNumber=phone;password=pwd;}
}
public class HostelSnapshot {
 public static void main(String[] args)throws Exception{
  Hostel h=new Hostel();h.hostelName="SIMATS Hostel";Room r=new Room();r.roomNo=101;
  r.residents.add(new Resident("Arun","192311001","9000000000","never-persist-this"));h.rooms.add(r);
  try(ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream("hostel.ser"))){out.writeObject(h);}
  Hostel loaded;try(ObjectInputStream in=new ObjectInputStream(new FileInputStream("hostel.ser"))){loaded=(Hostel)in.readObject();}
  Resident x=loaded.rooms.get(0).residents.get(0);
  System.out.println(loaded.hostelName+" | "+x.name+" | "+x.phoneNumber+" | password="+x.password);
  new File("hostel.ser").delete();
 }
}
