package vn.gasstation.integration.seenpro.online;

import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;

import java.util.List;

public class SeenProOnlineBootstrapIncompleteException extends LegacySystemUnavailableException {
    private final List<String> missingIdentifiers;

    public SeenProOnlineBootstrapIncompleteException(List<String> missingIdentifiers) {
        super("SeenPro online bootstrap thiếu identifier: " + String.join(", ", missingIdentifiers));
        this.missingIdentifiers = List.copyOf(missingIdentifiers);
    }

    public List<String> missingIdentifiers() {
        return missingIdentifiers;
    }
}
