package vn.gasstation.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTraceFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestTraceFilter.class);

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                              FilterChain chain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long started = System.nanoTime();
        RequestLogContext.open(requestId);
        response.setHeader("X-Request-Id", requestId);
        log.info("[HTTP] event=request.start method={} path={} clientIp={} origin={}", request.getMethod(),
            request.getRequestURI(), request.getRemoteAddr(), safe(request.getHeader("Origin")));
        try {
            chain.doFilter(request, response);
        } catch (RuntimeException | ServletException | IOException error) {
            RequestLogContext.warning();
            log.error("[ERROR] event=request.unhandled errorCode=INTERNAL_ERROR stage=request-processing rootCause={} requestId={} path={}",
                error.getClass().getSimpleName(), RequestLogContext.requestId(), request.getRequestURI());
            throw error;
        } finally {
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            var metrics = RequestLogContext.snapshot();
            log.info("[HTTP] event=request.completed method={} path={} status={} durationMs={} providerCalls={} cacheHits={} cacheMisses={} warnings={}",
                request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs,
                metrics.providerCalls(), metrics.cacheHits(), metrics.cacheMisses(), metrics.warnings());
            RequestLogContext.close();
        }
    }

    private static String safe(String value) { return value == null || value.isBlank() ? "-" : value; }
}

