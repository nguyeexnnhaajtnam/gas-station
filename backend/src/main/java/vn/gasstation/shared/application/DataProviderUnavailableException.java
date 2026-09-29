package vn.gasstation.shared.application;

public class DataProviderUnavailableException extends RuntimeException {
    public DataProviderUnavailableException(String message) { super(message); }
    public DataProviderUnavailableException(String message, Throwable cause) { super(message, cause); }
}

