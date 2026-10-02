package vn.gasstation.integration.seenpro.auth;

import org.junit.jupiter.api.Test;
import vn.gasstation.auth.domain.AuthenticationStatus;
import vn.gasstation.integration.seenpro.parser.SeenProCompanyParser;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class SeenProAuthenticationVerifierTest {
    private final SeenProAuthenticationVerifier verifier = new SeenProAuthenticationVerifier(new SeenProCompanyParser());
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

    @Test void companySectionDoesNotRequireLogoutControl() {
        var html = "<html><body><main><h1>CÔNG TY - ĐẠI LÝ</h1></main></body></html>";
        assertThat(verifier.verify(new SeenProAuthResponse(200, NO_HEADERS, html)))
            .isEqualTo(AuthenticationStatus.AUTHENTICATED);
    }

    @Test void parsedCompanyRowIsAcceptedWithoutTitleOrLogoutControl() throws IOException {
        try (var input = getClass().getResourceAsStream("/seenpro/company-list-success.html")) {
            if (input == null) throw new IOException("company fixture missing");
            var html = new String(input.readAllBytes(), StandardCharsets.UTF_8)
                .replace("<title>View</title>", "<title>Legacy page</title>");
            assertThat(verifier.verify(new SeenProAuthResponse(200, NO_HEADERS, html)))
                .isEqualTo(AuthenticationStatus.AUTHENTICATED);
        }
    }

    @Test void loginPageIsNotAuthenticated() {
        var html = "<html><body><form action='checklogin.php'><input type='password'></form></body></html>";
        assertThat(verifier.verify(new SeenProAuthResponse(200, NO_HEADERS, html)))
            .isEqualTo(AuthenticationStatus.UNVERIFIED);
    }

    @Test void companyPageRequiresHttp200() {
        var html = "<html><body><h1>CÔNG TY - ĐẠI LÝ</h1></body></html>";
        assertThat(verifier.verify(new SeenProAuthResponse(503, NO_HEADERS, html)))
            .isEqualTo(AuthenticationStatus.UNVERIFIED);
    }

    // REJECTED remains intentionally unused until an exact failed-login response is captured.
}
