import java.io.IOException;
class ExamException extends Exception {
    ExamException(String message) { super(message); }
    ExamException(String message, Throwable cause) { super(message, cause); }
}
class SubmissionStorageException extends ExamException {
    SubmissionStorageException(String message, Throwable cause) { super(message, cause); }
}
class ExamWindowClosedException extends ExamException {
    ExamWindowClosedException(String message) { super(message); }
}
class StudentNotRegisteredException extends ExamException {
    StudentNotRegisteredException(String message) { super(message); }
}
class Storage {
    void save(String fileName, long sizeKb) throws SubmissionStorageException {
        try {
            if (sizeKb > 500) throw new IOException("Disk quota exceeded");
            System.out.println("Saved " + fileName);
        } catch (IOException e) {
            throw new SubmissionStorageException("Could not store " + fileName, e);
        }
    }
}
class ExaminationService {
    void submit(String file, long size, boolean registered, boolean open) throws ExamException {
        if (!open) throw new ExamWindowClosedException("Exam window has closed");
        if (!registered) throw new StudentNotRegisteredException("Student is not registered");
        new Storage().save(file, size);
    }
}
public class StorageDemo {
    public static void main(String[] args) {
        try {
            ExaminationService service = new ExaminationService();
            service.submit("answer01.pdf", 120, true, true);
            service.submit("answer02.pdf", 800, true, true);
        } catch (ExamException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cause: " + e.getCause().getMessage());
        }
    }
}
