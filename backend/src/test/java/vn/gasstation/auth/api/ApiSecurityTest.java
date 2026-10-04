package vn.gasstation.auth.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.gasstation.auth.application.AccessTokenService;
import vn.gasstation.auth.application.AuthenticationService;
import vn.gasstation.auth.domain.AuthenticationStatus;
import vn.gasstation.infrastructure.config.SecurityConfig;
import vn.gasstation.pumpcolumn.api.PumpColumnController;
import vn.gasstation.pumpcolumn.application.PumpColumnService;

import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, PumpColumnController.class}, properties = "app.cors-origins=http://localhost:4200")
@Import({SecurityConfig.class, AccessTokenService.class})
class ApiSecurityTest {
    @Autowired MockMvc mvc;
    @Autowired AccessTokenService tokens;
    @MockitoBean AuthenticationService authentication;
    @MockitoBean PumpColumnService pumpColumns;

    @Test
    void rejectsApiCallsWithoutValidToken() throws Exception {
        mvc.perform(get("/api/v1/pump-columns")).andExpect(status().isUnauthorized())
            .andExpect(header().doesNotExist("WWW-Authenticate"));
        mvc.perform(get("/api/v1/pump-columns").header("Authorization", "Bearer forged")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/stations/abc/select")).andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsTokenIssuedAtLoginUntilLogout() throws Exception {
        when(authentication.authenticate("alice", "secret")).thenReturn(AuthenticationStatus.AUTHENTICATED);
        when(pumpColumns.findAll()).thenReturn(List.of());

        var body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"password\":\"secret\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", not(emptyOrNullString())))
            .andReturn().getResponse().getContentAsString();
        String token = body.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");

        mvc.perform(get("/api/v1/pump-columns").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/pump-columns").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectedLoginDoesNotIssueToken() throws Exception {
        when(authentication.authenticate("alice", "wrong")).thenReturn(AuthenticationStatus.REJECTED);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"password\":\"wrong\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").doesNotExist());
    }
}
