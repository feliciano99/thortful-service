package com.thortful.cards.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cardsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Thortful Greeting Cards API")
                .description("Server-side searchable, paginated greeting card catalogue")
                .version("v1"));
    }
}
