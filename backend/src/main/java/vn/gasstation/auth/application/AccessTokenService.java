package vn.gasstation.auth.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Opaque bearer tokens issued after a successful login. Kept in memory on purpose:
 * the SeenPro session they front is also in memory, so a restart invalidates both.
 * Tokens are never logged.
 */
@Service
public class AccessTokenService {
    private record Entry(String account, Instant expiresAt) {}

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Entry> tokens = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final Clock clock;

    @Autowired
    public AccessTokenService(@Value("${app.auth.token-ttl:12h}") Duration ttl) {
        this(ttl, Clock.systemUTC());
    }

    AccessTokenService(Duration ttl, Clock clock) {
        this.ttl = ttl;
        this.clock = clock;
    }

    /**
     * The backend holds a single SeenPro session, so tokens of a previously logged-in
     * account would otherwise read the new account's data: revoke them.
     */
    public String issue(String account) {
        var now = clock.instant();
        tokens.entrySet().removeIf(e -> !e.getValue().account().equals(account) || e.getValue().expiresAt().isBefore(now));
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(token, new Entry(account, now.plus(ttl)));
        return token;
    }

    public Optional<String> resolve(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        var entry = tokens.get(token);
        if (entry == null) return Optional.empty();
        if (entry.expiresAt().isBefore(clock.instant())) {
            tokens.remove(token);
            return Optional.empty();
        }
        return Optional.of(entry.account());
    }

    public void revoke(String token) {
        if (token != null) tokens.remove(token);
    }
}
