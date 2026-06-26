package com.thortful.cards.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thortful.cards.domain.CardNotFoundException;
import com.thortful.cards.domain.Category;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @BeforeEach
    void stubRequestPath() {
        when(request.getRequestURI()).thenReturn("/cards/v1/cards/99");
    }

    @Test
    void cardNotFoundMapsTo404() {
        ResponseEntity<ApiError> response = handler.handle(new CardNotFoundException(99L), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).contains("99");
        assertThat(response.getBody().path()).isEqualTo("/cards/v1/cards/99");
        assertThat(response.getBody().requestId()).isNull();
    }

    @Test
    void unmatchedResourceMapsTo404() {
        ResponseEntity<ApiError> response = handler.handle(new NoResourceFoundException(HttpMethod.GET, "/nope", "/nope"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Resource not found");
    }

    @Test
    void typeMismatchMapsTo400WithDescriptiveMessage() {
        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException("NONSENSE", Category.class, "category", null, null);

        ResponseEntity<ApiError> response = handler.handle(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("NONSENSE").contains("category");
    }

    @Test
    void unexpectedExceptionMapsTo500() {
        ResponseEntity<ApiError> response = handler.handleUnexpected(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
    }
}
