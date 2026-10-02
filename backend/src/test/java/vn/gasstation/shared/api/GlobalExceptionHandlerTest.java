package vn.gasstation.shared.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import vn.gasstation.integration.seenpro.dev.SeenProSessionNotEstablishedException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    @Test
    void mapsMissingSeenProSessionToExactUnauthorizedResponse() {
        var request = new MockHttpServletRequest("GET", "/api/v1/dev/seenpro/explorer/online-scripts");

        var response = new GlobalExceptionHandler().seenProSession(
            new SeenProSessionNotEstablishedException(), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("SeenPro session not established.");
    }
}
