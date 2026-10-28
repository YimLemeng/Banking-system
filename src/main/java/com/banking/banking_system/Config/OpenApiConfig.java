package com.banking.banking_system.Config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {
    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking System REST API")
                        .description("Production-grade Banking REST API with Spring Boot, PostgreSQL, JWT Authentication, and Role-based Authorization.")
                        .version("1.0.0")
                        .contact(new Contact().name("Banking Engineering Team").email("yimlemeng.ym@gmail.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public OperationCustomizer customizeSortParameters() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() != null) {
                for (Parameter parameter : operation.getParameters()) {
                    if ("sort".equalsIgnoreCase(parameter.getName())) {
                        parameter.setDescription("Sorting criteria: property(,asc|desc). Default sort order is descending. Example: id,desc or firstName,asc");
                        parameter.setExample(List.of("id,desc"));
                        if (parameter.getSchema() != null) {
                            parameter.getSchema().setExample(List.of("id,desc"));
                        }
                    }
                }
            }
            return operation;
        };
    }
}
