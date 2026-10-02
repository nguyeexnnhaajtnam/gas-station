package vn.gasstation.pump.api;

import org.junit.jupiter.api.Test;
import vn.gasstation.pump.domain.PumpRealtime;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PumpRealtimeResponseSecurityTest {
    @Test
    void publicRealtimeModelContainsNoSeenProTransportIdentifiers() {
        var fields = Arrays.stream(PumpRealtime.class.getRecordComponents())
            .map(component -> component.getName().toLowerCase())
            .toList();

        assertThat(fields).doesNotContain(
            "standardizedmac", "master", "slave", "fuelid", "user", "phpsessionid");
        assertThat(fields).containsExactly(
            "id", "number", "fueltype", "money", "liters", "unitprice", "totalizer",
            "connectionstatus", "operationalstatus", "nozzlestatus", "observedat");
    }
}
