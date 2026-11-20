package fr.snapwedding.shared.internal;

import fr.snapwedding.shared.ForbiddenException;
import fr.snapwedding.shared.InvalidRequestException;
import fr.snapwedding.shared.NotFoundException;
import fr.snapwedding.shared.SnapWeddingException;
import fr.snapwedding.shared.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Traduit les exceptions en réponses HTTP, toutes au format { "error": "..." }.
 * Hérite de ResponseEntityExceptionHandler pour garder les bons codes des erreurs Spring MVC
 * (JSON mal formé 400, fichier trop gros 413, mauvaise méthode 405...).
 */
@Slf4j
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(SnapWeddingException.class)
    public ResponseEntity<Map<String, String>> handleApplicationError(SnapWeddingException ex) {
        return switch (ex) {
            case NotFoundException e -> error(HttpStatus.NOT_FOUND, "Resource not found");
            case ForbiddenException e -> error(HttpStatus.FORBIDDEN, e.getMessage());
            case InvalidRequestException e -> error(HttpStatus.BAD_REQUEST, e.getMessage());
            case StorageException e -> {
                log.error("Object storage error", e);
                yield error(HttpStatus.INTERNAL_SERVER_ERROR, "File storage error");
            }
        };
    }

    /** @PreAuthorize refusé : 403, et surtout pas 500 via le handler générique. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "Access denied");
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("error", "Validation failed", "details", errors));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response == null) {
            return null;
        }
        String message = response.getBody() instanceof ProblemDetail problem && problem.getDetail() != null
                ? problem.getDetail()
                : "Request failed";
        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(Map.of("error", message));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleServerError(Exception ex) {
        log.error("Unexpected server error", ex);
        return Map.of("error", "Internal server error");
    }
}
