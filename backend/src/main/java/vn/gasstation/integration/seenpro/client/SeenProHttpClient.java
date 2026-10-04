package vn.gasstation.integration.seenpro.client;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.integration.seenpro.SeenProProperties;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
import vn.gasstation.infrastructure.logging.RequestLogContext;
import vn.gasstation.integration.seenpro.debug.SeenProHtmlCapture;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SeenProHttpClient {
    private static final Logger log = LoggerFactory.getLogger(SeenProHttpClient.class);
    private final SeenProProperties properties;
    private final SeenProSessionManager sessions;
    private final SeenProNavigationDiagnostics diagnostics;
    private SeenProHtmlCapture htmlCapture;
    public SeenProHttpClient(SeenProProperties properties, SeenProSessionManager sessions,
                             SeenProNavigationDiagnostics diagnostics) {
        this.properties = properties;
        this.sessions = sessions;
        this.diagnostics = diagnostics;
    }

    @Autowired
    void setHtmlCapture(SeenProHtmlCapture htmlCapture) { this.htmlCapture = htmlCapture; }

    public String get(String relativePath, Map<String, String> query) {
        try {
            var encoded = query.entrySet().stream().filter(e -> e.getValue() != null)
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue())).collect(Collectors.joining("&"));
            URI uri = properties.baseUrl().resolve(relativePath + (encoded.isEmpty() ? "" : "?" + encoded));
            var request = HttpRequest.newBuilder(uri).timeout(properties.readTimeout()).GET().build();
            var before = diagnostics.before(sessions);
            long started = System.nanoTime();
            RequestLogContext.providerCall();
            log.info("[SEENPRO] event=request provider=seenpro method=GET path={} queryKeys={}", relativePath, query.keySet());
            var response = sessions.client().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            capture(relativePath, response);
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            diagnostics.completed("GET", relativePath, response.statusCode(), response.headers(), response.body(),
                before, sessions, durationMs);
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                log.debug("[SECURITY] event=provider.authorization provider=seenpro status={} path={} sessionPreserved=true reason=session-expiration-contract-unconfirmed",
                    response.statusCode(), relativePath);
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) throw new LegacySystemUnavailableException("Nguồn dữ liệu kế thừa trả về lỗi HTTP " + response.statusCode());
            return response.body();
        } catch (LegacySystemUnavailableException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            providerError("GET", "request-interrupted", relativePath, e);
            throw new LegacySystemUnavailableException("Yêu cầu đọc nguồn dữ liệu kế thừa bị gián đoạn", e);
        } catch (Exception e) {
            providerError("GET", "request-failed", relativePath, e);
            throw new LegacySystemUnavailableException("Không thể đọc nguồn dữ liệu kế thừa", e);
        }
    }

    /** Executes a SeenPro form POST using the existing server-side cookie store. */
    public String postForm(String relativePath, Map<String, String> form) {
        try {
            String body = form.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
            URI uri = properties.baseUrl().resolve(relativePath);
            var request = HttpRequest.newBuilder(uri)
                .timeout(properties.readTimeout())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
            var before = diagnostics.before(sessions);
            long started = System.nanoTime();
            RequestLogContext.providerCall();
            log.info("[SEENPRO] event=request provider=seenpro method=POST path={} contentType=application/x-www-form-urlencoded formKeys={}",
                relativePath, form.keySet());
            var response = sessions.client().send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            capture(relativePath, response);
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            diagnostics.completed("POST", relativePath, response.statusCode(), response.headers(), response.body(),
                before, sessions, durationMs);
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                log.debug("[SECURITY] event=provider.authorization provider=seenpro status={} path={} sessionPreserved=true reason=session-expiration-contract-unconfirmed",
                    response.statusCode(), relativePath);
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new LegacySystemUnavailableException("SeenPro polling returned HTTP " + response.statusCode());
            }
            return response.body();
        } catch (LegacySystemUnavailableException error) {
            throw error;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            providerError("POST", "request-interrupted", relativePath, error);
            throw new LegacySystemUnavailableException("SeenPro polling request was interrupted", error);
        } catch (Exception error) {
            providerError("POST", "request-failed", relativePath, error);
            throw new LegacySystemUnavailableException("SeenPro polling request failed", error);
        }
    }

    public RedirectResponse getRedirect(String relativePath, Map<String, String> query) {
        var response = navigate(relativePath, query);
        return new RedirectResponse(response.statusCode(), response.location());
    }

    public NavigationResponse navigate(String relativePath, Map<String, String> query) {
        try {
            URI uri = uri(relativePath, query);
            var request = HttpRequest.newBuilder(uri).timeout(properties.readTimeout()).GET().build();
            var before = diagnostics.before(sessions);
            long started = System.nanoTime();
            RequestLogContext.providerCall();
            log.info("[SEENPRO] event=request provider=seenpro method=GET path={} stage=navigation queryKeys={}", relativePath, query.keySet());
            var response = sessions.client().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            capture(relativePath, response);
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            diagnostics.completed("GET", relativePath, response.statusCode(), response.headers(), response.body(),
                before, sessions, durationMs);
            if (response.statusCode() == 401 || response.statusCode() == 403) {
                log.debug("[SECURITY] event=provider.authorization provider=seenpro status={} path={} sessionPreserved=true reason=session-expiration-contract-unconfirmed",
                    response.statusCode(), relativePath);
            }
            String location = response.headers().firstValue("Location").orElse(null);
            return new NavigationResponse(response.statusCode(), location, response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            providerError("GET", "station-context-interrupted", relativePath, e);
            throw new LegacySystemUnavailableException("Yêu cầu kích hoạt ngữ cảnh trạm bị gián đoạn", e);
        } catch (Exception e) {
            providerError("GET", "station-context-failed", relativePath, e);
            throw new LegacySystemUnavailableException("Không thể kích hoạt ngữ cảnh trạm", e);
        }
    }

    public String getResource(String relativeReference) {
        try {
            URI uri = properties.baseUrl().resolve(relativeReference);
            URI base = properties.baseUrl();
            if (!sameOrigin(base, uri)) {
                log.debug("[SEENPRO] event=request.skipped provider=seenpro stage=bootstrap-resource reason=external-origin");
                return "";
            }
            var request = HttpRequest.newBuilder(uri).timeout(properties.readTimeout()).GET().build();
            var before = diagnostics.before(sessions);
            long started = System.nanoTime();
            RequestLogContext.providerCall();
            log.info("[SEENPRO] event=request provider=seenpro method=GET path={} stage=bootstrap-resource", uri.getPath());
            var response = sessions.client().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            capture(relativeReference, response);
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            diagnostics.completed("GET", uri.getPath(), response.statusCode(), response.headers(), response.body(),
                before, sessions, durationMs);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new LegacySystemUnavailableException("Không thể đọc JavaScript bootstrap SeenPro");
            }
            return response.body();
        } catch (LegacySystemUnavailableException error) {
            throw error;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new LegacySystemUnavailableException("Yêu cầu JavaScript bootstrap bị gián đoạn", error);
        } catch (Exception error) {
            throw new LegacySystemUnavailableException("Không thể đọc JavaScript bootstrap SeenPro", error);
        }
    }

    public boolean isSeenProOrigin(String reference) {
        if (reference == null || reference.isBlank() || !properties.baseUrlConfigured()) return false;
        try {
            return sameOrigin(properties.baseUrl(), properties.baseUrl().resolve(reference));
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public record RedirectResponse(int statusCode, String location) {}
    public record NavigationResponse(int statusCode, String location, String body) {}

    private URI uri(String relativePath, Map<String, String> query) {
        var encoded = query.entrySet().stream().filter(e -> e.getValue() != null)
            .map(e -> encode(e.getKey()) + "=" + encode(e.getValue())).collect(Collectors.joining("&"));
        return properties.baseUrl().resolve(relativePath + (encoded.isEmpty() ? "" : "?" + encoded));
    }
    private static boolean sameOrigin(URI base, URI target) {
        int basePort = base.getPort() == -1 ? defaultPort(base.getScheme()) : base.getPort();
        int targetPort = target.getPort() == -1 ? defaultPort(target.getScheme()) : target.getPort();
        return java.util.Objects.equals(base.getScheme(), target.getScheme())
            && java.util.Objects.equals(base.getHost(), target.getHost()) && basePort == targetPort;
    }
    private static int defaultPort(String scheme) { return "https".equalsIgnoreCase(scheme) ? 443 : 80; }
    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    private void capture(String relativePath, HttpResponse<String> response) {
        if (htmlCapture != null) htmlCapture.capture(relativePath, response.headers(), response.body());
    }
    private static void providerError(String method, String reason, String path, Exception error) {
        RequestLogContext.warning();
        log.error("[ERROR] event=provider.error provider=seenpro stage={} errorCode=DATA_PROVIDER_UNAVAILABLE rootCause={} requestId={} method={} path={}",
            reason, error.getClass().getSimpleName(), RequestLogContext.requestId(), method, path);
    }
}
