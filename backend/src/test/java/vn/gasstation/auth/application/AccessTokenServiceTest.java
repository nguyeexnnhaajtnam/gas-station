package vn.gasstation.auth.application;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-04T00:00:00Z");

    @Test
    void resolvesIssuedTokenUntilRevoked() {
        var service = new AccessTokenService(Duration.ofHours(1), Clock.fixed(NOW, ZoneOffset.UTC));
        String token = service.issue("alice");
        assertThat(service.resolve(token)).contains("alice");
        assertThat(service.resolve("unknown")).isEmpty();
        assertThat(service.resolve(null)).isEmpty();
        service.revoke(token);
        assertThat(service.resolve(token)).isEmpty();
    }

    @Test
    void rejectsExpiredToken() {
        var clock = new MutableClock(NOW);
        var service = new AccessTokenService(Duration.ofHours(1), clock);
        String token = service.issue("alice");
        clock.now = NOW.plus(Duration.ofMinutes(59));
        assertThat(service.resolve(token)).contains("alice");
        clock.now = NOW.plus(Duration.ofMinutes(61));
        assertThat(service.resolve(token)).isEmpty();
    }

    @Test
    void loginOfAnotherAccountRevokesPreviousAccountTokens() {
        var service = new AccessTokenService(Duration.ofHours(1), Clock.fixed(NOW, ZoneOffset.UTC));
        String alice = service.issue("alice");
        String alice2 = service.issue("alice");
        String bob = service.issue("bob");
        assertThat(service.resolve(alice)).isEmpty();
        assertThat(service.resolve(alice2)).isEmpty();
        assertThat(service.resolve(bob)).contains("bob");
    }

    private static final class MutableClock extends Clock {
        Instant now;
        MutableClock(Instant now) { this.now = now; }
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }
}
