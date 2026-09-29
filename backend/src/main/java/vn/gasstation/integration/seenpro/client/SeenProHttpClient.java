package vn.gasstation.integration.seenpro.client;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.integration.seenpro.SeenProProperties;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
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
    public SeenProHttpClient(SeenProProperties properties, SeenProSessionManager sessions) { this.properties = properties; this.sessions = sessions; }

    public String get(String relativePath, Map<String, String> query) {
        try {
            var encoded = query.entrySet().stream().filter(e -> e.getValue() != null)
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue())).collect(Collectors.joining("&"));
            URI uri = properties.baseUrl().resolve(relativePath + (encoded.isEmpty() ? "" : "?" + encoded));
            var request = HttpRequest.newBuilder(uri).timeout(properties.readTimeout()).GET().build();
            long started = System.nanoTime();
            log.info("seenpro.request started method=GET path={} queryKeys={}", relativePath, query.keySet());
            var response = sessions.client().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log.info("seenpro.request completed method=GET path={} status={} durationMs={} bodyChars={}", relativePath,
                response.statusCode(), (System.nanoTime()-started)/1_000_000, response.body().length());
            if (response.statusCode() == 401 || response.statusCode() == 403) sessions.invalidate();
            if (response.statusCode() < 200 || response.statusCode() >= 300) throw new LegacySystemUnavailableException("Nguồn dữ liệu kế thừa trả về lỗi HTTP " + response.statusCode());
            return response.body();
        } catch (LegacySystemUnavailableException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("seenpro.request interrupted method=GET path={} errorType={}", relativePath, e.getClass().getSimpleName());
            throw new LegacySystemUnavailableException("Yêu cầu đọc nguồn dữ liệu kế thừa bị gián đoạn", e);
        } catch (Exception e) {
            log.error("seenpro.request failed method=GET path={} errorType={}", relativePath, e.getClass().getSimpleName());
            throw new LegacySystemUnavailableException("Không thể đọc nguồn dữ liệu kế thừa", e);
        }
    }

    public RedirectResponse getRedirect(String relativePath, Map<String, String> query) {
        try {
            URI uri = uri(relativePath, query);
            var request = HttpRequest.newBuilder(uri).timeout(properties.readTimeout()).GET().build();
            long started = System.nanoTime();
            log.info("seenpro.request started method=GET path={} purpose=station-context queryKeys={}", relativePath, query.keySet());
            var response = sessions.client().send(request, HttpResponse.BodyHandlers.discarding());
            log.info("seenpro.request completed method=GET path={} purpose=station-context status={} durationMs={}",
                relativePath, response.statusCode(), (System.nanoTime() - started) / 1_000_000);
            if (response.statusCode() == 401 || response.statusCode() == 403) sessions.invalidate();
            String location = response.headers().firstValue("Location").orElse(null);
            return new RedirectResponse(response.statusCode(), location);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("seenpro.request interrupted method=GET path={} purpose=station-context errorType={}", relativePath, e.getClass().getSimpleName());
            throw new LegacySystemUnavailableException("Yêu cầu kích hoạt ngữ cảnh trạm bị gián đoạn", e);
        } catch (Exception e) {
            log.error("seenpro.request failed method=GET path={} purpose=station-context errorType={}", relativePath, e.getClass().getSimpleName());
            throw new LegacySystemUnavailableException("Không thể kích hoạt ngữ cảnh trạm", e);
        }
    }

    public record RedirectResponse(int statusCode, String location) {}

    private URI uri(String relativePath, Map<String, String> query) {
        var encoded = query.entrySet().stream().filter(e -> e.getValue() != null)
            .map(e -> encode(e.getKey()) + "=" + encode(e.getValue())).collect(Collectors.joining("&"));
        return properties.baseUrl().resolve(relativePath + (encoded.isEmpty() ? "" : "?" + encoded));
    }
    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    // POST is intentionally absent until a confirmed read-only use case requires it; mutating POSTs must never be retried.
}
