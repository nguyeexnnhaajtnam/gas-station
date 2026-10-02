package vn.gasstation.integration.seenpro.client;

import org.junit.jupiter.api.Test;
import vn.gasstation.integration.seenpro.online.SeenProPumpDescriptor;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SeenProPollingClientTest {
    @Test
    void usesTheConfirmedFormContractForEveryPollingEndpoint() {
        var http = mock(SeenProHttpClient.class);
        var sessions = mock(SeenProSessionManager.class);
        when(sessions.activeStationId()).thenReturn(Optional.of("station-id"));
        var descriptor = new SeenProPumpDescriptor(
            "CB01", "Cột 01", "AA:BB", "1", "2", "RON95", "operator",
            "connect-01", "pump-01", "idle");
        when(http.postForm(anyString(), anyMap())).thenAnswer(call -> call.getArgument(0));

        var payload = new SeenProPollingClient(http, sessions).fetchSnapshotInputs(descriptor);

        assertThat(payload.money()).isEqualTo("gettienhome.php");
        assertThat(payload.liters()).isEqualTo("getlithome.php");
        assertThat(payload.prices()).isEqualTo("getgiahome.php");
        assertThat(payload.totals()).isEqualTo("gettotal.php");
        assertThat(payload.connectionStates()).isEqualTo("getconnectstate.php");
        assertThat(payload.pumpStates()).isEqualTo("getpumpstate.php");
        verify(http).postForm("getconnectstate.php", Map.of(
            "master", "1", "maCot", "CB01", "standardizedMAC", "AA:BB"));
        verify(http).postForm("gettienhome.php", Map.of("standardizedMAC", "AA:BB"));
        verify(http).postForm("getlithome.php", Map.of("standardizedMAC", "AA:BB"));
        verify(http).postForm("getgiahome.php", Map.of("user", "operator", "maNhienLieu", "RON95"));
        verify(http).postForm("gettotal.php", Map.of("master", "1", "slave", "2"));
        verify(http).postForm("getpumpstate.php", Map.of("standardizedMAC", "AA:BB"));
        verify(http, never()).get(anyString(), anyMap());
    }

    @Test
    void keepsThePumpDescriptorWhenOnePollingMetricIsUnavailable() {
        var http = mock(SeenProHttpClient.class);
        var sessions = mock(SeenProSessionManager.class);
        when(sessions.activeStationId()).thenReturn(Optional.of("station-id"));
        var descriptor = new SeenProPumpDescriptor(
            "CB01", "Cột 01", "AA:BB", "1", "2", "RON95", "operator",
            "connect-01", "pump-01", "idle");
        when(http.postForm(eq("gettienhome.php"), anyMap()))
            .thenThrow(new LegacySystemUnavailableException("HTTP 500"));
        when(http.postForm(eq("getlithome.php"), anyMap())).thenReturn("getlithome.php");
        when(http.postForm(eq("getgiahome.php"), anyMap())).thenReturn("getgiahome.php");
        when(http.postForm(eq("gettotal.php"), anyMap())).thenReturn("gettotal.php");
        when(http.postForm(eq("getconnectstate.php"), anyMap())).thenReturn("getconnectstate.php");
        when(http.postForm(eq("getpumpstate.php"), anyMap())).thenReturn("images/pump-red.png");

        var payload = new SeenProPollingClient(http, sessions).fetchSnapshotInputs(descriptor);

        assertThat(payload.descriptor()).isSameAs(descriptor);
        assertThat(payload.money()).isNull();
        assertThat(payload.liters()).isEqualTo("getlithome.php");
        assertThat(payload.prices()).isEqualTo("getgiahome.php");
        assertThat(payload.totals()).isEqualTo("gettotal.php");
        assertThat(payload.connectionStates()).isEqualTo("getconnectstate.php");
        assertThat(payload.pumpStates()).isEqualTo("images/pump-red.png");
    }
}
