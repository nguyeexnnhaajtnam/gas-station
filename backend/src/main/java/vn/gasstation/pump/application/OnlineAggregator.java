package vn.gasstation.pump.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.infrastructure.logging.RequestLogContext;
import vn.gasstation.pump.domain.PumpRealtimeSnapshot;

import java.time.Duration;
import java.time.Instant;

@Service
public class OnlineAggregator {
    private static final Logger log = LoggerFactory.getLogger(OnlineAggregator.class);
    private final PumpRealtimeProvider provider;
    private final Duration cacheTtl;
    private volatile CachedSnapshot cached;

    public OnlineAggregator(PumpRealtimeProvider provider,
                            @Value("${app.realtime.cache-ttl:5s}") Duration cacheTtl) {
        this.provider = provider;
        this.cacheTtl = cacheTtl;
    }

    public PumpRealtimeSnapshot currentSnapshot() {
        CachedSnapshot current = cached;
        Instant now = Instant.now();
        if (current != null && now.isBefore(current.expiresAt())) {
            RequestLogContext.cacheHit();
            log.debug("[CACHE] event=cache.hit cache=pump-realtime count={}", current.snapshot().pumps().size());
            return current.snapshot();
        }
        RequestLogContext.cacheMiss();
        log.debug("[CACHE] event=cache.miss cache=pump-realtime");
        return refresh(now);
    }

    private synchronized PumpRealtimeSnapshot refresh(Instant now) {
        CachedSnapshot current = cached;
        if (current != null && now.isBefore(current.expiresAt())) {
            log.debug("[CACHE] event=cache.hit-after-lock cache=pump-realtime count={}", current.snapshot().pumps().size());
            return current.snapshot();
        }
        PumpRealtimeSnapshot snapshot = provider.currentSnapshot();
        cached = new CachedSnapshot(snapshot, now.plus(cacheTtl));
        log.debug("[CACHE] event=cache.store cache=pump-realtime count={} ttlMs={}",
            snapshot.pumps().size(), cacheTtl.toMillis());
        return snapshot;
    }

    private record CachedSnapshot(PumpRealtimeSnapshot snapshot, Instant expiresAt) {}
}
