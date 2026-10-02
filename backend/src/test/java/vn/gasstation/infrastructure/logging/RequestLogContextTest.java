package vn.gasstation.infrastructure.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLogContextTest {
    @AfterEach
    void cleanUp() {
        RequestLogContext.close();
    }

    @Test
    void keepsOneRequestIdAndAccumulatesSummaryMetrics() {
        RequestLogContext.open("request-123");
        RequestLogContext.providerCall();
        RequestLogContext.providerCall();
        RequestLogContext.cacheHit();
        RequestLogContext.cacheMiss();
        RequestLogContext.warning();

        assertThat(RequestLogContext.requestId()).isEqualTo("request-123");
        assertThat(RequestLogContext.snapshot()).isEqualTo(
            new RequestLogContext.Snapshot(2, 1, 1, 1));
    }

    @Test
    void closeRemovesRequestState() {
        RequestLogContext.open("request-123");
        RequestLogContext.close();

        assertThat(RequestLogContext.requestId()).isNull();
        assertThat(RequestLogContext.snapshot()).isEqualTo(
            new RequestLogContext.Snapshot(0, 0, 0, 0));
    }
}
