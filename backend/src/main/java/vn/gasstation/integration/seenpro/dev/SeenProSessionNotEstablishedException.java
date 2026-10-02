package vn.gasstation.integration.seenpro.dev;

public class SeenProSessionNotEstablishedException extends RuntimeException {
    public SeenProSessionNotEstablishedException() {
        super("SeenPro session not established.");
    }
}
