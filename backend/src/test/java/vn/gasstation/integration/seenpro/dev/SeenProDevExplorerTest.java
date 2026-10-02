package vn.gasstation.integration.seenpro.dev;

import org.junit.jupiter.api.Test;
import vn.gasstation.integration.seenpro.client.SeenProHttpClient;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SeenProDevExplorerTest {
    private final SeenProSessionManager sessions = mock(SeenProSessionManager.class);
    private final SeenProHttpClient client = mock(SeenProHttpClient.class);
    private final SeenProDevExplorer explorer = new SeenProDevExplorer(sessions, client);

    @Test
    void rejectsMissingRuntimeSessionWithoutCallingSeenPro() {
        when(sessions.activeStationId()).thenReturn(Optional.empty());
        when(sessions.authenticatedAccount()).thenReturn(Optional.of("runtime-account"));
        when(sessions.cookieSnapshot()).thenReturn(new SeenProSessionManager.CookieSnapshot(Map.of()));

        assertThatThrownBy(explorer::exploreOnlineJavaScript)
            .isInstanceOf(SeenProSessionNotEstablishedException.class)
            .hasMessage("SeenPro session not established.");
        verifyNoInteractions(client);
    }

    @Test
    void downloadsScriptsThroughExistingSessionClient() {
        when(sessions.activeStationId()).thenReturn(Optional.of("station-id"));
        when(sessions.authenticatedAccount()).thenReturn(Optional.empty());
        when(sessions.cookieSnapshot()).thenReturn(
            new SeenProSessionManager.CookieSnapshot(Map.of("PHPSESSID@seenpro/", 123)));
        when(client.get("online.php", Map.of())).thenReturn(
            "<html><head><title>Online</title>"
                + "<script src='https://code.jquery.com/jquery.js'></script>"
                + "<script src='js/online.js'></script>"
                + "<script>fetch('getpumpstate.php')</script></head></html>");
        when(client.isSeenProOrigin("https://code.jquery.com/jquery.js")).thenReturn(false);
        when(client.isSeenProOrigin("js/online.js")).thenReturn(true);
        when(client.getResource("js/online.js")).thenReturn("fetch('gettotal.php');");

        var result = explorer.exploreOnlineJavaScript();

        assertThat(result.entryPageTitle()).isEqualTo("Online");
        assertThat(result.seenProScripts()).singleElement().satisfies(script -> {
            assertThat(script.source()).isEqualTo("js/online.js");
            assertThat(script.characterCount()).isEqualTo(22);
            assertThat(script.sha256()).hasSize(64);
        });
        assertThat(result.inlineScripts()).singleElement().satisfies(block ->
            assertThat(block.index()).isZero());
        assertThat(result.skippedExternalScripts()).containsExactly("https://code.jquery.com/jquery.js");
        assertThat(result.ajaxEndpoints()).filteredOn(endpoint -> endpoint.endpoint().equals("gettotal"))
            .singleElement().satisfies(endpoint ->
                assertThat(endpoint.locations()).containsExactly("external:js/online.js"));
        assertThat(result.ajaxEndpoints()).filteredOn(endpoint -> endpoint.endpoint().equals("getpumpstate"))
            .singleElement().satisfies(endpoint ->
                assertThat(endpoint.locations()).containsExactly("inline:0"));
        verify(client, never()).getResource("https://code.jquery.com/jquery.js");
        verify(client, never()).get(eq("checklogin.php"), anyMap());
    }

    @Test
    void usesActiveStationContextInsteadOfLegacyAuthenticatedAccountMarker() {
        when(sessions.activeStationId()).thenReturn(Optional.of("station-id"));
        when(sessions.authenticatedAccount()).thenReturn(Optional.empty());
        when(sessions.cookieSnapshot()).thenReturn(
            new SeenProSessionManager.CookieSnapshot(Map.of("PHPSESSID@seenpro/", 123)));
        when(client.get("online.php", Map.of())).thenReturn("<html><title>Online</title></html>");

        assertThat(explorer.exploreOnlineJavaScript().entryPageTitle()).isEqualTo("Online");
        verify(client).get("online.php", Map.of());
    }
}
