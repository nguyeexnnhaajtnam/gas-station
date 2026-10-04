package vn.gasstation.auth.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.gasstation.auth.application.AccessTokenService;
import vn.gasstation.auth.application.AuthenticationService;
import vn.gasstation.auth.domain.AuthenticationStatus;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LoginResponse(AuthenticationStatus status, String message, String accessToken) {}

    private final AuthenticationService service;
    private final AccessTokenService tokens;
    public AuthController(AuthenticationService service, AccessTokenService tokens) {
        this.service = service;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var status = service.authenticate(request.username(), request.password());
        if (status == AuthenticationStatus.UNVERIFIED) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new LoginResponse(status,
                "Đã gửi yêu cầu đăng nhập; chưa thể xác minh kết quả từ hệ thống kế thừa", null));
        }
        if (status == AuthenticationStatus.AUTHENTICATED) {
            return ResponseEntity.ok(new LoginResponse(status, "Đăng nhập thành công", tokens.issue(request.username())));
        }
        return ResponseEntity.ok(new LoginResponse(status, "Thông tin đăng nhập không hợp lệ", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        tokens.revoke(BearerTokenFilter.extractToken(request));
        return ResponseEntity.noContent().build();
    }
}
