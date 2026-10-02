package vn.gasstation.integration.seenpro.session;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.Optional;

public interface SeenProSessionManager {
    HttpClient client();
    void markAuthenticated(String account);
    Optional<String> authenticatedAccount();
    void markStationContext(String stationId);
    Optional<String> activeStationId();
    CookieSnapshot cookieSnapshot();
    void invalidate();

    record CookieSnapshot(Map<String, Integer> fingerprints) {
        public CookieSnapshot {
            fingerprints = Map.copyOf(fingerprints);
        }
    }
}
