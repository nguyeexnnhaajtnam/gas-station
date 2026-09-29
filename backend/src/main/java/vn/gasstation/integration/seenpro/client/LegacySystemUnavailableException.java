package vn.gasstation.integration.seenpro.client;
import vn.gasstation.shared.application.DataProviderUnavailableException;
public class LegacySystemUnavailableException extends DataProviderUnavailableException {
    public LegacySystemUnavailableException(String message) { super(message); }
    public LegacySystemUnavailableException(String message, Throwable cause) { super(message, cause); }
}
