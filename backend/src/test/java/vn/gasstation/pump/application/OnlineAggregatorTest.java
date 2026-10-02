package vn.gasstation.pump.application;

import org.junit.jupiter.api.Test;
import vn.gasstation.pump.domain.PumpRealtimeSnapshot;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OnlineAggregatorTest {
    @Test
    void reusesOneAggregatedSnapshotWithinConfiguredTtl() {
        var provider = mock(PumpRealtimeProvider.class);
        var snapshot = PumpRealtimeSnapshot.unavailable(OffsetDateTime.now());
        when(provider.currentSnapshot()).thenReturn(snapshot);
        var aggregator = new OnlineAggregator(provider, Duration.ofSeconds(5));

        assertThat(aggregator.currentSnapshot()).isSameAs(snapshot);
        assertThat(aggregator.currentSnapshot()).isSameAs(snapshot);

        verify(provider, times(1)).currentSnapshot();
    }
}
