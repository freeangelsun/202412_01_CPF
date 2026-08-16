package com.cpf.bizadmin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BZA 공개 API의 실제 공통 예외 처리와 OpenAPI operation 응답을 일치시킨다.
 * 인증/인가/요청 바인딩/Rate Limit/서버 장애는 모든 공개 operation에 적용하고,
 * path 대상 부재는 404, 상태 변경 충돌은 409로 명시한다.
 */
@Configuration
public class BzaOpenApiErrorContractConfiguration {
    private static final Set<String> MUTATIONS = Set.of("post", "put", "patch", "delete");

    /** 공개 operation별 적용 가능한 오류 응답을 보강한다. */
    @Bean
    public OpenApiCustomizer cpfOperationErrorContractCustomizer() {
        return this::apply;
    }

    void apply(OpenAPI openApi) {
        ensureErrorComponents(openApi);
        if (openApi.getPaths() == null) return;
        openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap().forEach((method, operation) -> {
            if (!path.startsWith("/api/bza/")) return;
            String methodName = method.name().toLowerCase();
            List<String> statuses = new ArrayList<>(List.of("400", "401", "403", "429", "500", "503"));
            if (path.contains("{")) statuses.add("404");
            if (MUTATIONS.contains(methodName)) statuses.add("409");
            for (String status : statuses) operation.getResponses().putIfAbsent(status, response(status));
            operation.addExtension("x-cpf-applicable-error-statuses", List.copyOf(statuses));
        }));
    }

    private static void ensureErrorComponents(OpenAPI openApi) {
        if (openApi.getComponents() == null) openApi.setComponents(new io.swagger.v3.oas.models.Components());
        if (openApi.getComponents().getSchemas() == null) openApi.getComponents().setSchemas(new LinkedHashMap<>());
        if (!openApi.getComponents().getSchemas().containsKey("CpfApiError")) {
            ObjectSchema error = new ObjectSchema();
            error.addProperty("timestamp", new Schema<>().type("string").format("date-time"));
            error.addProperty("status", new Schema<>().type("integer").format("int32"));
            error.addProperty("error", new Schema<>().type("string"));
            error.addProperty("code", new Schema<>().type("string"));
            error.addProperty("message", new Schema<>().type("string"));
            error.addProperty("path", new Schema<>().type("string"));
            error.addProperty("transactionId", new Schema<>().type("string").minLength(34).maxLength(34));
            error.setRequired(List.of("status", "code", "message", "path"));
            openApi.getComponents().addSchemas("CpfApiError", error);
        }
    }

    private static ApiResponse response(String status) {
        Map<String, String> descriptions = Map.of(
                "400", "요청 형식/검증 실패", "401", "인증 필요", "403", "권한 부족",
                "404", "대상 없음", "409", "상태/동시성 충돌", "429", "호출 제한 초과",
                "500", "내부 처리 실패", "503", "일시적 서비스 불가");
        MediaType media = new MediaType().schema(new Schema<>().$ref("#/components/schemas/CpfApiError"));
        return new ApiResponse().description(descriptions.get(status))
                .content(new Content().addMediaType("application/json", media));
    }
}
