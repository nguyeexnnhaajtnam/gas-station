package vn.gasstation.auth.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.gasstation.auth.application.AccessTokenService;
import java.io.IOException;
import java.util.List;

/** Authenticates requests carrying "Authorization: Bearer &lt;token&gt;" issued by {@link AccessTokenService}. */
public class BearerTokenFilter extends OncePerRequestFilter {
    private static final String PREFIX = "Bearer ";
    private final AccessTokenService tokens;

    public BearerTokenFilter(AccessTokenService tokens) { this.tokens = tokens; }

    public static String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        return header != null && header.startsWith(PREFIX) ? header.substring(PREFIX.length()).trim() : null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        tokens.resolve(extractToken(request)).ifPresent(account -> SecurityContextHolder.getContext()
            .setAuthentication(UsernamePasswordAuthenticationToken.authenticated(account, null, List.of())));
        chain.doFilter(request, response);
    }
}
