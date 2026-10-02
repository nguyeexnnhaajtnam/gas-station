package vn.gasstation.integration.seenpro.session;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.integration.seenpro.SeenProProperties;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

@Component
public class InMemorySeenProSessionManager implements SeenProSessionManager {
    private static final Logger log = LoggerFactory.getLogger(InMemorySeenProSessionManager.class);
    private final SeenProProperties properties;
    private final CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private volatile HttpClient client;
    private volatile String authenticatedAccount;
    private volatile String activeStationId;

    public InMemorySeenProSessionManager(SeenProProperties properties) {
        this.properties = properties;
        log.debug("[SEENPRO] event=session.created provider=seenpro");
    }

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

    @Override public void markAuthenticated(String account) {
        this.authenticatedAccount = account;
        log.debug("[SECURITY] event=session.authenticated provider=seenpro cookieCount={}",
            cookies.getCookieStore().getCookies().size());
    }

    @Override public java.util.Optional<String> authenticatedAccount() {
        return java.util.Optional.ofNullable(authenticatedAccount);
    }

    @Override public void markStationContext(String stationId) {
        this.activeStationId = stationId;
        log.debug("[SEENPRO] event=session.station-context provider=seenpro stationId={} cookieCount={}",
            stationId, cookies.getCookieStore().getCookies().size());
    }

    @Override public java.util.Optional<String> activeStationId() {
        var result = java.util.Optional.ofNullable(activeStationId);
        log.debug("[SEENPRO] event=session.station-context.lookup provider=seenpro present={} stationId={}",
            result.isPresent(), result.orElse("-"));
        return result;
    }

    @Override public synchronized CookieSnapshot cookieSnapshot() {
        var fingerprints = new LinkedHashMap<String, Integer>();
        for (var cookie : cookies.getCookieStore().getCookies()) {
            String identity = cookie.getName() + "@" + cookie.getDomain() + cookie.getPath();
            fingerprints.put(identity, fingerprint(cookie.getValue()));
        }
        return new CookieSnapshot(fingerprints);
    }

    @Override public synchronized void invalidate() {
        log.debug("[SECURITY] event=session.invalidated provider=seenpro stationContextPresent={} cookieCount={}",
            activeStationId != null, cookies.getCookieStore().getCookies().size());
        cookies.getCookieStore().removeAll();
        authenticatedAccount = null;
        activeStationId = null;
        client = null;
    }

    private static int fingerprint(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            return java.nio.ByteBuffer.wrap(digest).getInt();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }
}
