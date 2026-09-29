package vn.gasstation.station.api;

import org.junit.jupiter.api.Test;
import vn.gasstation.station.application.StationService;
import vn.gasstation.station.domain.Station;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StationContextControllerTest {
    private final StationService service = mock(StationService.class);
    private final StationContextController controller = new StationContextController(service);

    @Test
    void returnsCleanStationResponseAfterActivation() {
        var station = new Station("station-id", "company-id", "public-code", "Trạm mẫu", null, null);
        when(service.activate("station-id")).thenReturn(Optional.of(station));

        var response = controller.select("station-id");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().station()).isEqualTo(StationResponse.from(station));
        assertThat(response.getBody().toString()).doesNotContain("PHPSESSID", "view.php", "menu.php", "gl=", "al=", "opt=");
    }

    @Test
    void unknownStationReturnsNotFound() {
        when(service.activate("unknown-id")).thenReturn(Optional.empty());
        assertThat(controller.select("unknown-id").getStatusCode().value()).isEqualTo(404);
    }
}
