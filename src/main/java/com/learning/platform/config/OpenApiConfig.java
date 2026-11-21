package com.learning.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Метаданные OpenAPI для Swagger UI (/swagger-ui.html) */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI learningPlatformOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Learning Platform API")
                .description("REST API учебной платформы: курсы, модули, уроки, задания, тесты, отзывы")
                .version("1.0.0"));
    }
}
