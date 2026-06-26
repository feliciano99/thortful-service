package com.thortful.cards.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.thortful.cards.TestcontainersConfiguration;
import com.thortful.cards.application.CategoryResponse;
import com.thortful.cards.application.CreateCardRequest;
import com.thortful.cards.application.GreetingCardResponse;
import com.thortful.cards.domain.Category;
import com.thortful.cards.domain.StockStatus;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class CardApiIT {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @LocalServerPort
    private int port;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void createThenSearchThenDelete() throws Exception {
        CreateCardRequest request =
                new CreateCardRequest("Api Lifecycle Card", Category.WEDDING, "Jane Doe", BigDecimal.valueOf(4.50), StockStatus.IN_STOCK);

        HttpResponse<String> created = post("/cards", request);
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(created.headers().firstValue("Location")).isPresent();
        GreetingCardResponse body = objectMapper.readValue(created.body(), GreetingCardResponse.class);
        Long id = body.id();

        HttpResponse<String> search = get("/cards?search=Lifecycle");
        assertThat(search.statusCode()).isEqualTo(200);
        assertThat(search.body()).contains("Api Lifecycle Card");

        HttpResponse<String> deleted = delete("/cards/" + id);
        assertThat(deleted.statusCode()).isEqualTo(204);

        HttpResponse<String> deletedAgain = delete("/cards/" + id);
        assertThat(deletedAgain.statusCode()).isEqualTo(404);
        ApiError error = objectMapper.readValue(deletedAgain.body(), ApiError.class);
        assertThat(error.requestId()).isNotBlank();
    }

    @Test
    void createInvalidReturnsValidationErrors() throws Exception {
        CreateCardRequest invalid =
                new CreateCardRequest("", Category.WEDDING, "Jane Doe", BigDecimal.valueOf(-1), null);

        HttpResponse<String> response = post("/cards", invalid);

        assertThat(response.statusCode()).isEqualTo(400);
        ApiError error = objectMapper.readValue(response.body(), ApiError.class);
        assertThat(error.fieldErrors())
                .extracting(FieldErrorDetail::field)
                .contains("title", "price", "stockStatus");
    }

    @Test
    void listsAllCategories() throws Exception {
        HttpResponse<String> response = get("/categories");

        assertThat(response.statusCode()).isEqualTo(200);
        CategoryResponse[] categories = objectMapper.readValue(response.body(), CategoryResponse[].class);
        assertThat(categories).hasSize(Category.values().length);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + contextPath + path);
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path) throws Exception {
        return http.send(HttpRequest.newBuilder(uri(path)).DELETE().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, Object payload) throws Exception {
        return http.send(
                HttpRequest.newBuilder(uri(path))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
