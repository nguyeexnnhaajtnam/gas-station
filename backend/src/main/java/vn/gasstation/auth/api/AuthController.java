package vn.gasstation.auth.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.gasstation.auth.application.AuthenticationService;
import vn.gasstation.auth.domain.AuthenticationStatus;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(AuthenticationStatus status, String message) {}

    private final AuthenticationService service;
    public AuthController(AuthenticationService service) { this.service = service; }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var status = service.authenticate(request.username(), request.password());
        if (status == AuthenticationStatus.UNVERIFIED) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new LoginResponse(status,
                "Đã gửi yêu cầu đăng nhập; chưa thể xác minh kết quả từ hệ thống kế thừa"));
        }
        return ResponseEntity.ok(new LoginResponse(status, status == AuthenticationStatus.AUTHENTICATED
            ? "Đăng nhập thành công" : "Thông tin đăng nhập không hợp lệ"));
    }
}
