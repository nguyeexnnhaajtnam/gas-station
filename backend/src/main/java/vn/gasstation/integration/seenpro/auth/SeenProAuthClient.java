package vn.gasstation.integration.seenpro.auth;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.integration.seenpro.SeenProProperties;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
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

    public SeenProAuthClient(SeenProProperties properties, SeenProSessionManager sessions, SeenProAuthMapper mapper) {
        this.properties = properties;
        this.sessions = sessions;
        this.mapper = mapper;
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
            long loginStarted = System.nanoTime();
            log.info("seenpro.request started method=POST path=/checklogin.php");
            var loginResponse = sessions.client().send(loginRequest, HttpResponse.BodyHandlers.discarding());
            log.info("seenpro.request completed method=POST path=/checklogin.php status={} durationMs={}",
                loginResponse.statusCode(), (System.nanoTime()-loginStarted)/1_000_000);

            String account = URLEncoder.encode(username, StandardCharsets.UTF_8);
            var verificationRequest = HttpRequest.newBuilder(properties.baseUrl().resolve(
                    "/view.php?gl=4&al=" + account + "&opt=v"))
                .timeout(properties.readTimeout())
                .GET()
                .build();
            long verificationStarted = System.nanoTime();
            log.info("seenpro.request started method=GET path=/view.php purpose=authentication-verification");
            var response = sessions.client().send(verificationRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            log.info("seenpro.request completed method=GET path=/view.php purpose=authentication-verification status={} durationMs={} bodyChars={}",
                response.statusCode(), (System.nanoTime()-verificationStarted)/1_000_000, response.body().length());
            return new SeenProAuthResponse(response.statusCode(), response.headers(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("seenpro.authentication interrupted errorType={}", e.getClass().getSimpleName());
            throw new LegacySystemUnavailableException("Yêu cầu đăng nhập nguồn dữ liệu kế thừa bị gián đoạn", e);
        } catch (Exception e) {
            log.error("seenpro.authentication failed errorType={}", e.getClass().getSimpleName());
            throw new LegacySystemUnavailableException("Không thể kết nối nguồn dữ liệu kế thừa", e);
        }
    }
}
