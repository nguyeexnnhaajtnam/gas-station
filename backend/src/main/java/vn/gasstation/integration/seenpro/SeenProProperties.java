package vn.gasstation.integration.seenpro;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.net.URI;
import java.time.Duration;

@ConfigurationProperties("app.seenpro")
public record SeenProProperties(URI baseUrl, Duration connectTimeout,
                                Duration readTimeout, Duration realtimePollInterval) {
    public boolean baseUrlConfigured() { return baseUrl != null; }
}
