package vn.gasstation.integration.seenpro.auth;

import org.junit.jupiter.api.Test;
import vn.gasstation.auth.domain.AuthenticationStatus;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class SeenProAuthenticationVerifierTest {
    private final SeenProAuthenticationVerifier verifier = new SeenProAuthenticationVerifier();
    private static final HttpHeaders NO_HEADERS = HttpHeaders.of(Map.of(), (a,b) -> true);

    @Test void authenticatedStructureIsAccepted() throws IOException {
        try (var input = getClass().getResourceAsStream("/seenpro/auth/authenticated-view-sanitized.html")) {
            if (input == null) throw new IOException("fixture missing");
            var html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(verifier.verify(new SeenProAuthResponse(200, NO_HEADERS, html)))
                .isEqualTo(AuthenticationStatus.AUTHENTICATED);
        }
    }

    @Test void unknownHtmlRemainsUnverifiedEvenWithHttp200() {
        var html = "<html><head><title>View</title></head><body>Unknown page</body></html>";
        assertThat(verifier.verify(new SeenProAuthResponse(200, NO_HEADERS, html)))
            .isEqualTo(AuthenticationStatus.UNVERIFIED);
    }

    // TODO add REJECTED coverage when a sanitized failed-login/login-page fixture is supplied.
}
