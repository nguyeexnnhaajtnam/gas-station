package vn.gasstation.integration.seenpro.auth;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.gasstation.auth.domain.AuthenticationStatus;
import vn.gasstation.integration.seenpro.parser.SeenProCompanyParser;
import java.util.Locale;

@Component
public class SeenProAuthenticationVerifier {
    private static final Logger log = LoggerFactory.getLogger(SeenProAuthenticationVerifier.class);
    private final SeenProCompanyParser companyParser;

    public SeenProAuthenticationVerifier(SeenProCompanyParser companyParser) {
        this.companyParser = companyParser;
    }

    public AuthenticationStatus verify(SeenProAuthResponse response) {
        if (response == null || response.body() == null || response.body().isBlank()) {
            log.info("[SECURITY] event=authentication.verified provider=seenpro result=UNVERIFIED reason=empty-body status={}",
                response == null ? "n/a" : response.statusCode());
            return AuthenticationStatus.UNVERIFIED;
        }
        if (response.statusCode() != 200) {
            log.info("[SECURITY] event=authentication.verified provider=seenpro result=UNVERIFIED reason=unexpected-status status={} responseSize={}",
                response.statusCode(), response.body().length());
            return AuthenticationStatus.UNVERIFIED;
        }

        var document = Jsoup.parse(response.body());
        var normalizedText = document.text().replaceAll("\\s+", " ").trim().toUpperCase(Locale.ROOT);
        boolean hasCompanySection = normalizedText.contains("CÔNG TY - ĐẠI LÝ");
        boolean hasParsedCompany = !companyParser.parse(response.body()).isEmpty();
        boolean loginPage = document.selectFirst("form[action*=checklogin.php], input[type=password]") != null
            || normalizedText.contains("ĐĂNG NHẬP");
        boolean accessDenied = normalizedText.contains("ACCESS DENIED")
            || normalizedText.contains("TRUY CẬP BỊ TỪ CHỐI");

        boolean companyPage = hasCompanySection || hasParsedCompany;
        var status = (companyPage && !loginPage && !accessDenied)
            ? AuthenticationStatus.AUTHENTICATED
            : AuthenticationStatus.UNVERIFIED;

        log.info(
            "[SECURITY] event=authentication.verified provider=seenpro result={} status={} companyPage={} parsedCompanies={} loginPage={} accessDenied={} responseSize={}",
            status, response.statusCode(), hasCompanySection, hasParsedCompany, loginPage, accessDenied,
            response.body().length());

        return status;
    }
}
