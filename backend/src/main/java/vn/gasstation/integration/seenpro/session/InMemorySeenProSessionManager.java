package vn.gasstation.integration.seenpro.session;

import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.SeenProProperties;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;

@Component
public class InMemorySeenProSessionManager implements SeenProSessionManager {
    private final SeenProProperties properties;
    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private volatile HttpClient client;
    private volatile String authenticatedAccount;
    private volatile String activeStationId;

    public InMemorySeenProSessionManager(SeenProProperties properties) { this.properties = properties; }

    @Override public synchronized HttpClient client() {
        if (client == null) {
            client = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .connectTimeout(properties.connectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        }
        return client;
    }

    @Override public void markAuthenticated(String account) { this.authenticatedAccount = account; }

    @Override public java.util.Optional<String> authenticatedAccount() {
        return java.util.Optional.ofNullable(authenticatedAccount);
    }

    @Override public void markStationContext(String stationId) { this.activeStationId = stationId; }

    @Override public java.util.Optional<String> activeStationId() {
        return java.util.Optional.ofNullable(activeStationId);
    }

    @Override public synchronized void invalidate() {
        cookies.getCookieStore().removeAll();
        authenticatedAccount = null;
        activeStationId = null;
        client = null;
    }
}
