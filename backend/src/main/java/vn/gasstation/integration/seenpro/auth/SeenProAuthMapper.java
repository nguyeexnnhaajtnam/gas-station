package vn.gasstation.integration.seenpro.auth;

import org.springframework.stereotype.Component;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class SeenProAuthMapper {
    public String toFormBody(String username, String password) {
        return "taikhoan=" + encode(username)
            + "&matkhau=" + encode(password)
            + "&btn-submit=";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

