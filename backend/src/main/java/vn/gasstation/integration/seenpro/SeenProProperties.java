package vn.gasstation.integration.seenpro;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.net.URI;
import java.time.Duration;

@ConfigurationProperties("app.seenpro")
public record SeenProProperties(URI baseUrl, String username, String password, Duration connectTimeout,
                                Duration readTimeout, Duration realtimePollInterval) {
    public boolean configured() { return baseUrl != null && username != null && !username.isBlank() && password != null && !password.isBlank(); }
    public boolean baseUrlConfigured() { return baseUrl != null; }
}
