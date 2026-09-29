package vn.gasstation.integration.seenpro.station;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.gasstation.integration.seenpro.client.LegacySystemUnavailableException;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;
import vn.gasstation.station.domain.Station;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SeenProStationContextManagerTest {
    private SeenProHttpClient client;
    private SeenProSessionManager sessions;
    private SeenProStationReferenceRegistry references;
    private SeenProStationContextManager manager;
    private Station station;

    @BeforeEach
    void setUp() {
        client = mock(SeenProHttpClient.class);
        sessions = mock(SeenProSessionManager.class);
        references = new SeenProStationReferenceRegistry();
        manager = new SeenProStationContextManager(client, sessions, references);
        station = new Station("station-id", "company-id", "public-code", "Trạm mẫu", null, null);
        references.register(station, "legacy_station_ref");
        when(sessions.authenticatedAccount()).thenReturn(Optional.of("authenticated-account"));
    }

    @Test
    void activatesKnownStationUsingInternalReferenceAndConfirmedRedirect() {
        when(client.getRedirect("view.php", query())).thenReturn(new SeenProHttpClient.RedirectResponse(302, "menu.php"));

        assertThat(manager.activate("station-id")).contains(station);
        verify(client).getRedirect("view.php", query());
    }

    @Test
    void acceptsAbsoluteMenuLocation() {
        when(client.getRedirect("view.php", query())).thenReturn(new SeenProHttpClient.RedirectResponse(302, "http://seenpro.example/menu.php"));
        assertThat(manager.activate("station-id")).contains(station);
    }

    @Test
    void rejectsUnexpectedRedirect() {
        when(client.getRedirect("view.php", query())).thenReturn(new SeenProHttpClient.RedirectResponse(302, "index.php"));
        assertThatThrownBy(() -> manager.activate("station-id")).isInstanceOf(LegacySystemUnavailableException.class);
    }

    @Test
    void doesNotTreatHttp200LoginLikeResponseAsSuccess() {
        when(client.getRedirect("view.php", query())).thenReturn(new SeenProHttpClient.RedirectResponse(200, null));
        assertThatThrownBy(() -> manager.activate("station-id")).isInstanceOf(LegacySystemUnavailableException.class);
    }

    @Test
    void unknownStationReturnsEmptyWithoutCallingSeenPro() {
        assertThat(manager.activate("unknown-id")).isEmpty();
        verifyNoInteractions(client);
    }

    @Test
    void unauthenticatedSessionIsRejectedBeforeRequest() {
        when(sessions.authenticatedAccount()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> manager.activate("station-id")).isInstanceOf(LegacySystemUnavailableException.class);
        verifyNoInteractions(client);
    }

    private Map<String, String> query() {
        return Map.of("gl", "2", "al", "legacy_station_ref", "opt", "v");
    }
}
