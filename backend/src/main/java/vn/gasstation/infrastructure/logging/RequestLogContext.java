package vn.gasstation.infrastructure.logging;

import org.slf4j.MDC;

/** Per-request logging metrics. Synchronous provider calls inherit the request MDC/thread context. */
public final class RequestLogContext {
    private static final ThreadLocal<Metrics> CURRENT = new ThreadLocal<>();

    private RequestLogContext() {}

    public static void open(String requestId) {
        CURRENT.set(new Metrics(requestId));
        MDC.put("requestId", requestId);
    }

    public static String requestId() {
        Metrics metrics = CURRENT.get();
        return metrics == null ? MDC.get("requestId") : metrics.requestId;
    }

    public static void providerCall() { if (CURRENT.get() != null) CURRENT.get().providerCalls++; }
    public static void cacheHit() { if (CURRENT.get() != null) CURRENT.get().cacheHits++; }
    public static void cacheMiss() { if (CURRENT.get() != null) CURRENT.get().cacheMisses++; }
    public static void warning() { if (CURRENT.get() != null) CURRENT.get().warnings++; }

    public static Snapshot snapshot() {
        Metrics value = CURRENT.get();
        return value == null ? new Snapshot(0, 0, 0, 0)
            : new Snapshot(value.providerCalls, value.cacheHits, value.cacheMisses, value.warnings);
    }

    public static void close() {
        CURRENT.remove();
        MDC.remove("requestId");
    }

    private static final class Metrics {
        private final String requestId;
        private int providerCalls;
        private int cacheHits;
        private int cacheMisses;
        private int warnings;

        private Metrics(String requestId) { this.requestId = requestId; }
    }

    public record Snapshot(int providerCalls, int cacheHits, int cacheMisses, int warnings) {}
}
