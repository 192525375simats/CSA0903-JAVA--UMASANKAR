import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PassStore {
 private final Path file;
 public PassStore(Path file){this.file=file;}
 public void add(String passId,String regNo,String name,int route,double amount)throws IOException{
  if(passId.isBlank()||regNo.isBlank()||name.isBlank()||route<0||amount<0)throw new IllegalArgumentException("Invalid pass fields");
  String[] fields={passId,regNo,name,String.valueOf(route),String.valueOf(amount)};
  for(String field:fields)if(field.contains(",")||field.contains("\n")||field.contains("\r"))throw new IllegalArgumentException("CSV comma/newline is not supported by this simple assignment format");
  try(BufferedWriter w=Files.newBufferedWriter(file,StandardOpenOption.CREATE,StandardOpenOption.APPEND)){
   w.write(String.join(",",fields));w.newLine();
  }
 }
 /** Returns five fields or null when the ID is absent. Damaged records are skipped. */
 public String[] find(String passId)throws IOException{
  try(BufferedReader r=Files.newBufferedReader(file)){
   String line;
   while((line=r.readLine())!=null){
    String[] fields=line.split(",",-1);
    if(fields.length!=5)continue;
    try{Integer.parseInt(fields[3]);double amount=Double.parseDouble(fields[4]);if(!Double.isFinite(amount)||amount<0)continue;}
    catch(NumberFormatException ex){continue;}
    if(fields[0].equals(passId))return fields;
   }
  }catch(NoSuchFileException ex){return null;}
  return null;
 }
 public List<String[]> listByRoute(int route)throws IOException{
  List<String[]> out=new ArrayList<>();
  if(!Files.exists(file))return out;
  try(BufferedReader r=Files.newBufferedReader(file)){
   String line;while((line=r.readLine())!=null){
    String[] f=line.split(",",-1);if(f.length!=5)continue;
    try{if(Integer.parseInt(f[3])==route){double a=Double.parseDouble(f[4]);if(Double.isFinite(a)&&a>=0)out.add(f);}}
    catch(NumberFormatException ignored){}
   }
  }return out;
 }
 public static void main(String[]args)throws IOException{
  Path f=Files.createTempFile("passes",".csv");PassStore s=new PassStore(f);
  s.add("P1001","192311001","Arun",12,4500);
  Files.writeString(f,"P1002,DAMAGED LINE\nP1004,192311004,InvalidAmount,12,abc\n",StandardOpenOption.APPEND);
  s.add("P1003","192311003","Divya",7,3800);
  System.out.println(Arrays.toString(s.find("P1003")));
  System.out.println(s.find("P1002"));
  System.out.println("Route 12 count="+s.listByRoute(12).size());
  Files.deleteIfExists(f);
 }
}
