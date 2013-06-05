package info.guardianproject.pgpbenchmark;

public class Progress {

    public Progress(String message, int current, int total) {
        this.current = current;
        this.total = total;
        this.message = message;
    }
    public int current;
    public int total;
    public String message;

    public long elapsed;
}
