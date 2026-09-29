package vn.gasstation.integration.seenpro.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.gasstation.auth.application.AuthenticationProvider;
import vn.gasstation.auth.domain.AuthenticationStatus;
import vn.gasstation.integration.seenpro.auth.SeenProAuthClient;
import vn.gasstation.integration.seenpro.auth.SeenProAuthenticationVerifier;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;

@Component
@ConditionalOnProperty(name="app.data-source", havingValue="seenpro", matchIfMissing=true)
public class SeenProAuthenticationProvider implements AuthenticationProvider {
    private final SeenProAuthClient client;
    private final SeenProAuthenticationVerifier verifier;
    private final SeenProSessionManager sessions;

    public SeenProAuthenticationProvider(SeenProAuthClient client, SeenProAuthenticationVerifier verifier,
                                         SeenProSessionManager sessions) {
        this.client = client;
        this.verifier = verifier;
        this.sessions = sessions;
    }

    @Override public AuthenticationStatus authenticate(String username, String password) {
        var status = verifier.verify(client.authenticateAndFetchVerificationPage(username, password));
        if (status == AuthenticationStatus.AUTHENTICATED) sessions.markAuthenticated(username);
        return status;
    }
}
