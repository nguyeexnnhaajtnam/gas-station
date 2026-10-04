package vn.gasstation.integration.seenpro.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;

import java.net.http.HttpHeaders;

@Component
public class SeenProNavigationDiagnostics {
    private static final Logger log = LoggerFactory.getLogger(SeenProNavigationDiagnostics.class);

    public SeenProSessionManager.CookieSnapshot before(SeenProSessionManager sessions) {
        return sessions.cookieSnapshot();
    }

    public void completed(String method, String path, int status, HttpHeaders headers, String body,
                          SeenProSessionManager.CookieSnapshot before, SeenProSessionManager sessions,
                          long durationMs) {
        String location = headers.firstValue("Location").orElse("-");
        boolean changed = !before.equals(sessions.cookieSnapshot());
        log.info("[SEENPRO] event=response provider=seenpro method={} path={} status={} durationMs={} responseSize={} redirect={} cookieStoreChanged={}",
            method, path, status, durationMs, body == null ? 0 : body.length(), location, changed);
    }

}
