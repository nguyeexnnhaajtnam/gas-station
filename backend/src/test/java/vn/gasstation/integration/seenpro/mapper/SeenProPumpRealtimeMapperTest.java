package vn.gasstation.integration.seenpro.mapper;

import org.junit.jupiter.api.Test;
import vn.gasstation.integration.seenpro.model.SeenProOnlinePayload;
import vn.gasstation.integration.seenpro.online.SeenProPumpDescriptor;
import vn.gasstation.pump.domain.PumpRealtime;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeenProPumpRealtimeMapperTest {
    @Test
    void aggregatesSixResponsesForTheSameDescriptor() {
        var descriptor = new SeenProPumpDescriptor("CB01", "Cột 01", "AA", "1", "2",
            "legacy-fuel-id", "user", "connect-01", "pump-01", "dispensing");
        var payload = new SeenProOnlinePayload(descriptor, "500000", "25,12", "19900", "250761,39", "online", "dispensing");

        var snapshot = new SeenProPumpRealtimeMapper().map(List.of(payload));

        assertThat(snapshot.available()).isTrue();
        assertThat(snapshot.pumps()).singleElement().satisfies(pump -> {
            assertThat(pump.id()).isEqualTo("CB01");
            assertThat(pump.number()).isEqualTo("Cột 01");
            assertThat(pump.money()).isEqualByComparingTo(new BigDecimal("500000"));
            assertThat(pump.liters()).isEqualByComparingTo(new BigDecimal("25.12"));
            assertThat(pump.unitPrice()).isEqualByComparingTo(new BigDecimal("19900"));
            assertThat(pump.totalizer()).isEqualByComparingTo(new BigDecimal("250761.39"));
            assertThat(pump.connectionStatus()).isEqualTo(PumpRealtime.ConnectionStatus.ONLINE);
            assertThat(pump.operationalStatus()).isEqualTo(PumpRealtime.OperationalStatus.FUELING);
            assertThat(pump.fuelType()).isEqualTo("UNKNOWN");
        });
    }

    @Test
    void mapsSeenProPumpImageResponsesWithoutExposingTheirPaths() {
        var descriptor = new SeenProPumpDescriptor(
            "CB01", "Cá»™t 01", "AA:BB", "1", "2", "RON95", "operator",
            "connect-01", "pump-01", "idle");
        var payload = new SeenProOnlinePayload(
            descriptor, "0", "0", "0", "0", "online", "images/pump-red.png");

        var pump = new SeenProPumpRealtimeMapper().map(List.of(payload)).pumps().get(0);

        assertThat(pump.operationalStatus()).isEqualTo(PumpRealtime.OperationalStatus.FUELING);
        assertThat(pump.nozzleStatus()).isEqualTo(PumpRealtime.NozzleStatus.DISPENSING);
    }
}
