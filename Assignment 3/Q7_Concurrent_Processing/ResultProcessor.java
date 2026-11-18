import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
public class ResultProcessor {
    record Result(String file, boolean success, String detail) {}
    static Result process(Path file) {
        try {
            String value = Files.readString(file).trim();
            int score = Integer.parseInt(value);
            if (score < 0 || score > 100) throw new IllegalArgumentException("Score out of range");
            return new Result(file.getFileName().toString(), true, "Result=" + score);
        } catch (IOException e) { return new Result(file.getFileName().toString(), false, e.getClass().getSimpleName()); }
          catch (IllegalArgumentException e) { return new Result(file.getFileName().toString(), false, e.getClass().getSimpleName()); }
    }
    public static void main(String[] args) throws Exception {
        Path dir = Path.of("scores"); Files.createDirectories(dir);
        for(int i=1;i<=8;i++) Files.writeString(dir.resolve("student"+i+".txt"), String.valueOf(50+i*5));
        Files.writeString(dir.resolve("student9.txt"), "corrupt-score");
        List<Path> tasks = new ArrayList<>();
        try(var stream=Files.list(dir)){stream.forEach(tasks::add);}
        tasks.add(dir.resolve("student10.txt"));
        ExecutorService pool=Executors.newFixedThreadPool(4);
        List<Future<Result>> futures=new ArrayList<>();
        for(Path p:tasks) futures.add(pool.submit(() -> process(p)));
        List<Result> results=new ArrayList<>(); for(Future<Result> f:futures) results.add(f.get());
        pool.shutdown();
        long ok=results.stream().filter(Result::success).count();
        System.out.println("Files processed: "+results.size());
        System.out.println("Successful: "+ok);
        System.out.println("Failed: "+(results.size()-ok));
        for(Result r:results) if(!r.success()) System.out.println("Failure: "+r.file()+" - "+r.detail());
    }
}
