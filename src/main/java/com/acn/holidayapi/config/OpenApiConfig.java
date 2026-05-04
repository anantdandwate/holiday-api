package com.acn.holidayapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Holiday API")
                        .version("1.0.0")
                        .description("REST API for public holiday information using Nager.Date API")
                        .contact(new Contact()
                                .name("ACN Team")
                                .email("support@acn.com")))
                .servers(Arrays.asList(
                        new Server().url(baseUrl).description("Current Environment"),
                        new Server().url("http://localhost:8080").description("Local Development")));
    }
}