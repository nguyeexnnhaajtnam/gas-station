package vn.gasstation.shared.api;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.gasstation.infrastructure.logging.RequestLogContext;
import vn.gasstation.integration.seenpro.dev.SeenProSessionNotEstablishedException;
import vn.gasstation.shared.application.DataProviderUnavailableException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException error, HttpServletRequest request) {
        var fields = error.getBindingResult().getFieldErrors().stream()
            .map(item -> new ApiError.FieldViolation(item.getField(), item.getDefaultMessage())).toList();
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Dữ liệu không hợp lệ",
            request, fields, error);
    }

    @ExceptionHandler(DataProviderUnavailableException.class)
    ResponseEntity<ApiError> providerUnavailable(DataProviderUnavailableException error,
                                                   HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "DATA_PROVIDER_UNAVAILABLE", error.getMessage(),
            request, List.of(), error);
    }

    @ExceptionHandler(SeenProSessionNotEstablishedException.class)
    ResponseEntity<ApiError> seenProSession(SeenProSessionNotEstablishedException error,
                                             HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "SEENPRO_SESSION_NOT_ESTABLISHED", error.getMessage(),
            request, List.of(), error);
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                               HttpServletRequest request,
                                               List<ApiError.FieldViolation> fields, Throwable error) {
        RequestLogContext.warning();
        log.error("[ERROR] event=request.error errorCode={} stage={} rootCause={} requestId={} status={} path={}",
            code, stage(code), rootCause(error).getClass().getSimpleName(), RequestLogContext.requestId(),
            status.value(), request.getRequestURI());
        return ResponseEntity.status(status).body(new ApiError(
            Instant.now(), status.value(), code, message, request.getRequestURI(), fields));
    }

    private static String stage(String code) {
        if (code.startsWith("SEENPRO") || code.equals("DATA_PROVIDER_UNAVAILABLE")) {
            return "seenpro-integration";
        }
        if (code.equals("VALIDATION_FAILED")) return "request-validation";
        return "request-processing";
    }

    private static Throwable rootCause(Throwable error) {
        Throwable result = error;
        while (result.getCause() != null && result.getCause() != result) result = result.getCause();
        return result;
    }
}
