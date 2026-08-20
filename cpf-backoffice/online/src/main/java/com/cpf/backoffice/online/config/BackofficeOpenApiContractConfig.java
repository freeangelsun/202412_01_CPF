package com.cpf.backoffice.online.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** MBW runtime OpenAPI에 공통 보안/오류 계약을 fail-closed로 주입합니다. */
@Configuration
public class BackofficeOpenApiContractConfig {
    private static final String ERROR_SCHEMA = "#/components/schemas/CpfApiError";

    @Bean
    OpenApiCustomizer MBW_STANDARD_ERROR_RESPONSES() {
        return openApi -> {
            Components components = openApi.getComponents() == null ? new Components() : openApi.getComponents();
            openApi.setComponents(components);
            if (components.getSchemas() == null || !components.getSchemas().containsKey("CpfApiError")) {
                ObjectSchema error = new ObjectSchema();
                error.addProperty("timestamp", new StringSchema().format("date-time"));
                error.addProperty("status", new IntegerSchema().format("int32"));
                error.addProperty("error", new StringSchema());
                error.addProperty("code", new StringSchema());
                error.addProperty("message", new StringSchema());
                error.addProperty("path", new StringSchema());
                error.addProperty("transactionId", new StringSchema().minLength(34).maxLength(34));
                error.required(java.util.List.of("status", "code", "message", "path"));
                components.addSchemas("CpfApiError", error);
            }
            if (openApi.getPaths() == null) return;
            openApi.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, op) -> {
                add(op.getResponses(), "401", "Authentication required");
                add(op.getResponses(), "403", "Permission denied");
                add(op.getResponses(), "429", "Rate limit exceeded");
                add(op.getResponses(), "500", "Internal server error");
                add(op.getResponses(), "503", "Service temporarily unavailable");
                if (path.contains("{")) add(op.getResponses(), "404", "Resource not found");
                switch (method) {
                    case POST, PUT, PATCH, DELETE -> { add(op.getResponses(), "409", "State/version conflict"); add(op.getResponses(), "422", "Validation or semantic request failure"); }
                    default -> { }
                }
            }));
        };
    }

    private static void add(io.swagger.v3.oas.models.responses.ApiResponses responses, String status, String description) {
        if (responses == null || responses.containsKey(status)) return;
        MediaType json = new MediaType().schema(new io.swagger.v3.oas.models.media.Schema<>().$ref(ERROR_SCHEMA));
        responses.addApiResponse(status, new ApiResponse().description(description).content(new Content().addMediaType("application/json", json)));
    }
}
