package ca.gbc.roomservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Value("${room-service.version}")
    private String version;

    @Bean
    public OpenAPI roomServiceAPI(){
        return new OpenAPI()
                .info(new Info().title("Room Service API")
                        .description("This is the REST API for Room Service")
                        .version(version)
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Room Service REST API - GBC -COMP3095 - 2024")
                        .url("https://mycompany.ca/room-service/docs"));
    }
}
