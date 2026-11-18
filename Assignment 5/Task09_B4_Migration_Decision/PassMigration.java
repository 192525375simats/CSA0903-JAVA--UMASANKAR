import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
public class PassMigration {
 public static void main(String[]args)throws Exception{
  Path input=Path.of(args.length>0?args[0]:"passes.csv");List<String[]> valid=new ArrayList<>();List<String> failed=new ArrayList<>();
  try(BufferedReader r=Files.newBufferedReader(input)){String line;int lineNo=0;while((line=r.readLine())!=null){lineNo++;String[]p=line.split(",",-1);
   try{if(p.length!=5)throw new IllegalArgumentException("expected five fields");if(p[0].isBlank()||p[1].isBlank()||p[2].isBlank())throw new IllegalArgumentException("empty required field");int route=Integer.parseInt(p[3]);double amount=Double.parseDouble(p[4]);if(route<0||!Double.isFinite(amount)||amount<0)throw new IllegalArgumentException("invalid route/amount");valid.add(p);}
   catch(RuntimeException ex){failed.add("line "+lineNo+": "+ex.getMessage()+" | "+line);}
  }}
  String url=System.getenv().getOrDefault("DB_URL","jdbc:mysql://localhost:3306/assignment5");
  try(Connection c=DriverManager.getConnection(url,System.getenv().getOrDefault("DB_USER","root"),System.getenv().getOrDefault("DB_PASSWORD",""))){
   int loaded=PassLoader.loadAll(c,valid);System.out.println("Valid rows loaded: "+loaded);
   System.out.println("Rejected rows: "+failed.size());for(String x:failed)System.out.println("REJECTED "+x);
   DatabaseMetaData md=c.getMetaData();System.out.println("Database: "+md.getDatabaseProductName()+" "+md.getDatabaseProductVersion());System.out.println("Driver: "+md.getDriverName()+" "+md.getDriverVersion());
   try(ResultSet tables=md.getTables(c.getCatalog(),null,"%",new String[]{"TABLE"})){System.out.println("Tables:");while(tables.next())System.out.println(" - "+tables.getString("TABLE_NAME"));}
  }
 }
}
