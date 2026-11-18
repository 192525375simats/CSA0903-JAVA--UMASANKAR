import java.nio.file.*;
import java.util.*;
public class PassStoreTest {
 public static void main(String[]args)throws Exception{
  Path p=Files.createTempFile("pass-test",".csv");PassStore s=new PassStore(p);
  s.add("P1","R1","Asha",4,2500);
  Files.writeString(p,"wrong,field,count\nP2,R2,Malformed,4,not-a-number\n\n",StandardOpenOption.APPEND);
  s.add("P3","R3","Kiran",4,2600);
  System.out.println("Normal find: "+Arrays.toString(s.find("P1")));
  System.out.println("Missing ID: "+s.find("P404"));
  System.out.println("Damaged P2: "+s.find("P2"));
  System.out.println("Route 4 records: "+s.listByRoute(4).size());
  Files.deleteIfExists(p);
 }
}
