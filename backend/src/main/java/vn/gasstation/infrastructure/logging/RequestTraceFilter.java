package vn.gasstation.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);
        log.info("http.request started method={} path={}", request.getMethod(), request.getRequestURI());
        try {
            chain.doFilter(request, response);
        } catch (Exception error) {
            log.error("http.request failed method={} path={} errorType={}", request.getMethod(),
                request.getRequestURI(), error.getClass().getSimpleName());
            throw error;
        } finally {
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            log.info("http.request completed method={} path={} status={} durationMs={}", request.getMethod(),
                request.getRequestURI(), response.getStatus(), durationMs);
            MDC.remove("requestId");
        }
    }
}

