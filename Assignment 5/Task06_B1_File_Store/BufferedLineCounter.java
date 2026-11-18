import java.io.*;
import java.nio.file.*;
public class BufferedLineCounter {
 static long countUnbuffered(Path p)throws IOException{long n=0;try(InputStream in=Files.newInputStream(p)){int b;while((b=in.read())!=-1)if(b=='\n')n++;}return n;}
 static long countBuffered(Path p)throws IOException{long n=0;byte[] buffer=new byte[64*1024];try(InputStream in=new BufferedInputStream(Files.newInputStream(p),buffer.length)){int len;while((len=in.read(buffer))!=-1)for(int i=0;i<len;i++)if(buffer[i]=='\n')n++;}return n;}
 public static void main(String[]a)throws Exception{
  for(int mb:new int[]{1,10,50}){Path p=Files.createTempFile("attendance",".log");try(OutputStream o=Files.newOutputStream(p)){byte[] block=("attendance-record\n").getBytes();long target=mb*1024L*1024L;for(long written=0;written<target;written+=block.length)o.write(block);}
   long t=System.nanoTime(),u=countUnbuffered(p),tu=System.nanoTime()-t;t=System.nanoTime();long b=countBuffered(p),tb=System.nanoTime()-t;
   System.out.printf("%d MiB | lines=%d/%d | unbuffered=%.2f ms | buffered=%.2f ms%n",mb,u,b,tu/1e6,tb/1e6);Files.delete(p);
  }
 }
}
