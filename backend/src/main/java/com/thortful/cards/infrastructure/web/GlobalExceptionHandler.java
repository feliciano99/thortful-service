package com.thortful.cards.infrastructure.web;

import com.thortful.cards.domain.CardNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            CardNotFoundException.class,
            NoResourceFoundException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> handle(Exception exception, HttpServletRequest request) {
        return switch (exception) {
            case CardNotFoundException e ->
                    error(HttpStatus.NOT_FOUND, e.getMessage(), request, List.of());
            case NoResourceFoundException _ ->
                    error(HttpStatus.NOT_FOUND, "Resource not found", request, List.of());
            case MethodArgumentNotValidException e ->
                    error(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors(e));
            case MethodArgumentTypeMismatchException e ->
                    error(HttpStatus.BAD_REQUEST, typeMismatchMessage(e), request, List.of());
            case HttpMessageNotReadableException _ ->
                    error(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body", request, List.of());
            default ->
                    error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, List.of());
        };
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request, List.of());
    }

    private static List<FieldErrorDetail> fieldErrors(MethodArgumentNotValidException exception) {
        return exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
    }

    private static String typeMismatchMessage(MethodArgumentTypeMismatchException exception) {
        return "Invalid value '" + exception.getValue() + "' for parameter '" + exception.getName() + "'";
    }

    private static ResponseEntity<ApiError> error(
            HttpStatus status, String message, HttpServletRequest request, List<FieldErrorDetail> fieldErrors) {
        ApiError body = new ApiError(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                RequestContext.currentRequestId(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
