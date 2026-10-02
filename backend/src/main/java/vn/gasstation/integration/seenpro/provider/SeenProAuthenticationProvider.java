package vn.gasstation.integration.seenpro.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.gasstation.auth.application.AuthenticationProvider;
import vn.gasstation.auth.domain.AuthenticationStatus;
import vn.gasstation.integration.seenpro.auth.SeenProAuthClient;
import vn.gasstation.integration.seenpro.auth.SeenProAuthenticationVerifier;
import vn.gasstation.integration.seenpro.session.SeenProSessionManager;

@Component
@ConditionalOnProperty(name="app.data-source", havingValue="seenpro", matchIfMissing=true)
public class SeenProAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(SeenProAuthenticationProvider.class);
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
        log.info("[BUSINESS] event=authentication.start provider=seenpro");
        var status = verifier.verify(client.authenticateAndFetchVerificationPage(username, password));
        if (status == AuthenticationStatus.AUTHENTICATED) sessions.markAuthenticated(username);
        log.info("[BUSINESS] event=authentication.completed provider=seenpro result={} sessionEstablished={}",
            status, sessions.authenticatedAccount().isPresent());
        return status;
    }
}
