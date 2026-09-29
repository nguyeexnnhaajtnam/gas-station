package vn.gasstation.integration.seenpro.auth;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import vn.gasstation.auth.domain.AuthenticationStatus;
import java.util.Locale;

@Component
public class SeenProAuthenticationVerifier {
    public AuthenticationStatus verify(SeenProAuthResponse response) {
        if (response == null || response.body() == null || response.body().isBlank()) {
            return AuthenticationStatus.UNVERIFIED;
        }

        var document = Jsoup.parse(response.body());
        var normalizedText = document.text().replaceAll("\\s+", " ").trim().toUpperCase(Locale.ROOT);
        boolean hasViewTitle = "View".equalsIgnoreCase(document.title().trim());
        boolean hasCompanySection = normalizedText.contains("CÔNG TY - ĐẠI LÝ");
        boolean hasLogoutControl = document.select("a[href]").stream().anyMatch(link -> {
            String href = link.attr("href").trim().toLowerCase(Locale.ROOT);
            String hrefPath = href.split("[?#]", 2)[0];
            String label = link.text().replaceAll("\\s+", " ").trim().toLowerCase(Locale.forLanguageTag("vi"));
            return (hrefPath.equals("index.php") || hrefPath.endsWith("/index.php"))
                && (label.contains("đăng xuất") || label.contains("logout"));
        });

        boolean successfulHttpResponse = response.statusCode() >= 200 && response.statusCode() < 300;
        if (successfulHttpResponse && hasViewTitle && hasCompanySection && hasLogoutControl) {
            return AuthenticationStatus.AUTHENTICATED;
        }

        // No failed-login structure has been confirmed yet. Do not guess REJECTED.
        return AuthenticationStatus.UNVERIFIED;
    }
}
