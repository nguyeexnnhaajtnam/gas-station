package vn.gasstation.shared.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.gasstation.shared.application.DataProviderUnavailableException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var fields = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new ApiError.FieldViolation(e.getField(), e.getDefaultMessage())).toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Dữ liệu không hợp lệ", req, fields);
    }

    @ExceptionHandler(DataProviderUnavailableException.class)
    ResponseEntity<ApiError> legacy(DataProviderUnavailableException ex, HttpServletRequest req) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "DATA_PROVIDER_UNAVAILABLE", ex.getMessage(), req, List.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                               HttpServletRequest req, List<ApiError.FieldViolation> fields) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), code, message, req.getRequestURI(), fields));
    }
}
