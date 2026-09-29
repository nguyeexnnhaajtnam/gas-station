package vn.gasstation.auth.application;
import org.springframework.stereotype.Service;
import vn.gasstation.auth.domain.AuthenticationStatus;
@Service
public class AuthenticationService {
    private final AuthenticationProvider provider;
    public AuthenticationService(AuthenticationProvider provider) { this.provider = provider; }
    public AuthenticationStatus authenticate(String username, String password) { return provider.authenticate(username, password); }
}
