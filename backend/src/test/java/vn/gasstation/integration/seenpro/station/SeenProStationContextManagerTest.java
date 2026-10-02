package vn.gasstation.integration.seenpro.station;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
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
        manager = new SeenProStationContextManager(client, sessions, references, new SeenProStationContextVerifier());
        station = new Station("station-id", "company-id", "public-code", "Trạm mẫu", null, null);
        references.register(station, "legacy_station_ref");
        when(sessions.authenticatedAccount()).thenReturn(Optional.of("authenticated-account"));
        when(sessions.cookieSnapshot()).thenReturn(new SeenProSessionManager.CookieSnapshot(Map.of()));
        when(client.navigate(anyString(), eq(Map.of())))
            .thenReturn(new SeenProHttpClient.NavigationResponse(200, null,
                "<html><header>Trạm mẫu</header></html>"));
    }

    @Test
    void activatesKnownStationUsingInternalReferenceAndConfirmedRedirect() {
        when(client.getRedirect("view.php", query())).thenReturn(new SeenProHttpClient.RedirectResponse(302, "menu.php"));

        assertThat(manager.activate("station-id")).contains(station);
        InOrder navigation = inOrder(client);
        navigation.verify(client).getRedirect("view.php", query());
        navigation.verify(client).navigate("menu.php", Map.of());
        navigation.verify(client).navigate("quanlycuahang.php", Map.of());
        navigation.verify(client).navigate("online.php", Map.of());
        verify(sessions).markStationContext("station-id");
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

    @Test
    void doesNotMarkContextWhenStorePageHeaderBelongsToAnotherStation() {
        when(client.getRedirect("view.php", query())).thenReturn(new SeenProHttpClient.RedirectResponse(302, "menu.php"));
        when(client.navigate("menu.php", Map.of()))
            .thenReturn(new SeenProHttpClient.NavigationResponse(200, null, "<html></html>"));
        when(client.navigate("quanlycuahang.php", Map.of()))
            .thenReturn(new SeenProHttpClient.NavigationResponse(200, null, "<header>Trạm khác</header>"));

        assertThatThrownBy(() -> manager.activate("station-id")).isInstanceOf(LegacySystemUnavailableException.class);
        verify(sessions, never()).markStationContext(anyString());
        verify(client, never()).navigate("online.php", Map.of());
    }

    @Test
    void doesNotMarkContextWhenOnlinePageLosesSelectedStation() {
        when(client.getRedirect("view.php", query())).thenReturn(new SeenProHttpClient.RedirectResponse(302, "menu.php"));
        when(client.navigate("menu.php", Map.of()))
            .thenReturn(new SeenProHttpClient.NavigationResponse(200, null, "<html></html>"));
        when(client.navigate("quanlycuahang.php", Map.of()))
            .thenReturn(new SeenProHttpClient.NavigationResponse(200, null, "<header>Trạm mẫu</header>"));
        when(client.navigate("online.php", Map.of()))
            .thenReturn(new SeenProHttpClient.NavigationResponse(200, null, "<header>Trạm khác</header>"));

        assertThatThrownBy(() -> manager.activate("station-id")).isInstanceOf(LegacySystemUnavailableException.class);
        verify(sessions, never()).markStationContext(anyString());
    }

    private Map<String, String> query() {
        return Map.of("gl", "2", "al", "legacy_station_ref", "opt", "v");
    }
}
