package vn.gasstation.integration.seenpro.auth;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.integration.seenpro.SeenProProperties;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.client.SeenProNavigationDiagnostics;
import vn.gasstation.integration.seenpro.debug.SeenProHtmlCapture;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
import vn.gasstation.infrastructure.logging.RequestLogContext;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class SeenProAuthClient {
    private static final Logger log = LoggerFactory.getLogger(SeenProAuthClient.class);
    private final SeenProProperties properties;
    private final SeenProSessionManager sessions;
    private final SeenProAuthMapper mapper;
    private final SeenProNavigationDiagnostics diagnostics;
    private final SeenProHtmlCapture htmlCapture;

    public SeenProAuthClient(SeenProProperties properties, SeenProSessionManager sessions, SeenProAuthMapper mapper,
                             SeenProNavigationDiagnostics diagnostics, SeenProHtmlCapture htmlCapture) {
        this.properties = properties;
        this.sessions = sessions;
        this.mapper = mapper;
        this.diagnostics = diagnostics;
        this.htmlCapture = htmlCapture;
    }

    public SeenProAuthResponse authenticateAndFetchVerificationPage(String username, String password) {
        if (!properties.baseUrlConfigured()) {
            throw new LegacySystemUnavailableException("Chưa cấu hình địa chỉ nguồn dữ liệu kế thừa");
        }
        // A new attempt must not inherit a previously authenticated session.
        sessions.invalidate();
        var loginRequest = HttpRequest.newBuilder(properties.baseUrl().resolve("/checklogin.php"))
            .timeout(properties.readTimeout())
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(
                mapper.toFormBody(username, password), StandardCharsets.UTF_8))
            .build();
        try {
            // The login response body/status is not used as proof of authentication.
            var loginCookiesBefore = diagnostics.before(sessions);
            long loginStarted = System.nanoTime();
            RequestLogContext.providerCall();
            log.info("[SEENPRO] event=request provider=seenpro method=POST path=/checklogin.php stage=authentication");
            var loginResponse = sessions.client().send(loginRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            htmlCapture.capture("checklogin.php", loginResponse.headers(), loginResponse.body());
            long loginDurationMs = (System.nanoTime()-loginStarted)/1_000_000;
            diagnostics.completed("POST", "/checklogin.php", loginResponse.statusCode(), loginResponse.headers(), loginResponse.body(),
                loginCookiesBefore, sessions, loginDurationMs);

            String account = URLEncoder.encode(username, StandardCharsets.UTF_8);
            var verificationRequest = HttpRequest.newBuilder(properties.baseUrl().resolve(
                    "/view.php?gl=4&al=" + account + "&opt=v"))
                .timeout(properties.readTimeout())
                .GET()
                .build();
            long verificationStarted = System.nanoTime();
            var verificationCookiesBefore = diagnostics.before(sessions);
            RequestLogContext.providerCall();
            log.info("[SEENPRO] event=request provider=seenpro method=GET path=/view.php stage=authentication-verification");
            var response = sessions.client().send(verificationRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            htmlCapture.capture("view.php", response.headers(), response.body());
            long verificationDurationMs = (System.nanoTime()-verificationStarted)/1_000_000;
            diagnostics.completed("GET", "/view.php", response.statusCode(), response.headers(), response.body(),
                verificationCookiesBefore, sessions, verificationDurationMs);
            return new SeenProAuthResponse(response.statusCode(), response.headers(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            authenticationError("interrupted", e);
            throw new LegacySystemUnavailableException("Yêu cầu đăng nhập nguồn dữ liệu kế thừa bị gián đoạn", e);
        } catch (Exception e) {
            authenticationError("request", e);
            throw new LegacySystemUnavailableException("Không thể kết nối nguồn dữ liệu kế thừa", e);
        }
    }

    private static void authenticationError(String stage, Exception error) {
        RequestLogContext.warning();
        log.error("[ERROR] event=provider.error provider=seenpro stage=authentication-{} errorCode=DATA_PROVIDER_UNAVAILABLE rootCause={} requestId={}",
            stage, error.getClass().getSimpleName(), RequestLogContext.requestId());
    }
}
