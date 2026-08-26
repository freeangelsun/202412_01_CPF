package com.cpf.web.internal.openapi;

import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.core.api.version.CpfPlatformVersion;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.context.CpfOperationIdResolver;
import com.cpf.web.api.openapi.CpfOpenAPIOperations;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.time.Clock;
import java.util.stream.Collectors;

/**
 * CPF API 문서 자동 설정입니다.
 *
 * <p>업무 모듈이 별도 Swagger 설정을 작성하지 않아도 공통 거래 헤더와 API 그룹이
 * 자동으로 노출되도록 구성합니다. 각 업무 모듈은 Controller의 tag, summary,
 * request/response 예시만 보강하면 CPF 표준 문서 형태를 유지할 수 있습니다.</p>
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@EnableConfigurationProperties(CpfOpenApiProperties.class)
public class CpfOpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI cpfOpenAPI(Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", "cpf-service").toUpperCase(java.util.Locale.ROOT);
        return new OpenAPI()
                .info(new Info()
                        .title("CPF " + applicationName + " API")
                        .version(environment.getProperty("cpf.openapi.version", CpfPlatformVersion.unknown().componentVersion()))
                        .description("Core Platform Framework 표준 API 문서입니다.")
                        .contact(new Contact()
                                .name("CPF Framework Team")
                                .email("cpf-framework@example.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("CPF 프레임워크 문서")
                        .url("/docs"))
                .components(new Components().addSchemas("CpfErrorResponse", cpfErrorResponseSchema()));
    }

    @Bean
    public GroupedOpenApi cpfAllApiGroup() {
        return GroupedOpenApi.builder()
                .group("00-ALL API")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "cpfSwaggerUiHtmlRedirectFilter")
    public FilterRegistrationBean<Filter> cpfSwaggerUiHtmlRedirectFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setName("cpfSwaggerUiHtmlRedirectFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        registration.setFilter((request, response, chain) -> {
            if (request instanceof HttpServletRequest httpRequest
                    && response instanceof HttpServletResponse httpResponse
                    && isSwaggerUiHtmlRequest(httpRequest)) {
                // springdoc의 legacy 진입점이 환경에 따라 오류를 내도 CPF 공식 경로로 일관되게 보냅니다.
                httpResponse.setStatus(HttpServletResponse.SC_FOUND);
                httpResponse.setHeader("Location", httpRequest.getContextPath() + "/swagger-ui/index.html");
                return;
            }
            chain.doFilter(request, response);
        });
        return registration;
    }

    @Bean
    public OperationCustomizer cpfTransactionHeaderOperationCustomizer() {
        return (operation, handlerMethod) -> {
            OnlineTransactionMetadata online = findOnlineTransaction(handlerMethod);
            if (online != null) {
                // External Channel을 포함한 모든 업무 HTTP 경계는 Canonical 6을 함께 전달합니다.
                addHeader(operation, CpfHttpHeaderNames.TRANSACTION_ID, true,
                        "Required end-to-end CPF transaction ID. Internal CPF hops propagate it automatically.");
                addHeader(operation, CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE, true,
                        "Required System Code that first created/started this transaction. Immutable for the transaction lifetime.");
                addHeader(operation, CpfHttpHeaderNames.SYSTEM_CODE, true,
                        "Required current processing System Code for this hop. Runtime validates it against the receiver System Code.");
                addHeader(operation, CpfHttpHeaderNames.CALLER_SYSTEM_CODE, true,
                        "Required immediate caller System Code for this hop.");
                addHeader(operation, CpfHttpHeaderNames.TARGET_SYSTEM_CODE, true,
                        "Required target System Code. Runtime validates it against the receiver System Code.");
                addHeader(operation, CpfHttpHeaderNames.TARGET_OPERATION_ID, true,
                        "Required canonical target operationId. Runtime validates it against the resolved handler before Controller execution.");
                addChannelHeader(operation, CpfHttpHeaderNames.CALLER_CHANNEL, false,
                        "Optional Channel policy context. It is not a substitute for Caller System Code.");
            }
            addHeader(operation, CpfHttpHeaderNames.COUNTRY_CODE, false, "Client/service country code when supplied by contract.");
            addHeader(operation, CpfHttpHeaderNames.CLIENT_ID, false, "Client/application identifier; not a security authority by itself.");
            addHeader(operation, CpfHttpHeaderNames.CLIENT_INSTANCE_ID, false, "Client installation/runtime instance identifier.");
            addHeader(operation, CpfHttpHeaderNames.CLIENT_VERSION, false, "Client/application version.");
            addHeader(operation, CpfHttpHeaderNames.DEVICE_ID, false, "Device identifier subject to masking policy.");
            addHeader(operation, CpfHttpHeaderNames.TRACEPARENT, false, "W3C distributed traceparent.");
            addHeader(operation, CpfHttpHeaderNames.CORRELATION_ID, false, "Optional external correlation identifier.");
            addHeader(operation, CpfHttpHeaderNames.IDEMPOTENCY_KEY, false, "Idempotency key when the operation contract uses one.");
            return operation;
        };
    }

    private OnlineTransactionMetadata findOnlineTransaction(HandlerMethod handlerMethod) {
        CpfOnlineTransaction standard = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), CpfOnlineTransaction.class);
        if (standard == null) standard = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), CpfOnlineTransaction.class);
        return standard == null ? null : new OnlineTransactionMetadata(standard.operationId(), standard.name());
    }


    @Bean
    @ConditionalOnMissingBean(CpfOpenAPIOperations.class)
    public CpfOpenAPIOperations cpfOpenApiOperations(CpfOpenApiProperties properties,
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mappings) {
        return new DefaultCpfOpenAPIOperations(properties, mappings, Clock.systemUTC());
    }

    /**
     * 명시적 operationId가 없는 API에도 재현 가능한 식별자를 부여합니다.
     *
     * <p>개발자가 업무 의미가 담긴 operationId를 선언하면 그 값을 우선 사용합니다. 선언하지 않은 경우에는
     * Controller, 메서드, 파라미터 타입을 조합하므로 메서드 오버로드도 서로 다른 ID를 가집니다.</p>
     */
    @Bean
    public OperationCustomizer cpfOperationIdCustomizer(CpfOperationIdResolver resolver) {
        return (operation, handlerMethod) -> {
            String canonical = resolver.resolve(handlerMethod);
            if (operation.getOperationId() == null || operation.getOperationId().isBlank()) {
                operation.setOperationId(canonical);
            }
            return operation;
        };
    }

    @Bean
    public OperationCustomizer cpfStandardErrorResponseCustomizer() {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }
            addErrorResponse(responses, "400", "요청 형식 또는 필수값 오류");
            addErrorResponse(responses, "401", "인증 정보 누락 또는 만료");
            addErrorResponse(responses, "403", "요청 권한 부족");
            addErrorResponse(responses, "404", "대상 리소스 없음");
            addErrorResponse(responses, "409", "중복 또는 현재 상태 충돌");
            addErrorResponse(responses, "422", "의미 검증 실패");
            addErrorResponse(responses, "429", "호출 한도 초과");
            addErrorResponse(responses, "500", "서버 내부 처리 오류");
            addErrorResponse(responses, "502", "외부 연계 실패");
            addErrorResponse(responses, "503", "일시적 서비스 사용 불가");
            return operation;
        };
    }

    private Schema<?> cpfErrorResponseSchema() {
        return new ObjectSchema()
                .description("CPF 표준 오류 응답입니다. 내부 예외와 민감정보는 노출하지 않습니다.")
                .addProperty("messageId", new StringSchema().description("오류 메시지 식별자"))
                .addProperty("transactionId", new StringSchema().description("트랜잭션 글로벌 ID"))
                .addProperty("traceId", new StringSchema().description("분산 추적 ID"))
                .addProperty("statusCode", new StringSchema().description("CPF 표준 응답코드"))
                .addProperty("messageCode", new StringSchema().description("다국어 메시지코드"))
                .addProperty("message", new StringSchema().description("외부 공개 오류 메시지"))
                .addProperty("messageContent", new StringSchema().description("외부 공개 오류 메시지 본문"))
                .addProperty("errorDetail", new ObjectSchema().description("노출 가능한 정제 오류 부가정보"))
                .addProperty("timestamp", new DateTimeSchema().description("오류 발생 일시"));
    }

    private void addErrorResponse(ApiResponses responses, String statusCode, String description) {
        if (responses.containsKey(statusCode)) {
            return;
        }
        io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/CpfErrorResponse"));
        responses.addApiResponse(statusCode, new ApiResponse()
                .description(description)
                .content(new io.swagger.v3.oas.models.media.Content()
                        .addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType)));
    }

    String generatedOperationId(org.springframework.web.method.HandlerMethod handlerMethod) {
        String controller = handlerMethod.getBeanType().getSimpleName().replaceFirst("Controller$", "");
        String method = capitalize(handlerMethod.getMethod().getName());
        String parameters = Arrays.stream(handlerMethod.getMethod().getParameterTypes())
                .map(Class::getSimpleName)
                .map(this::capitalize)
                .collect(Collectors.joining("And"));
        String base = lowerFirst(controller) + method;
        return parameters.isBlank() ? base : base + "Using" + parameters;
    }

    private boolean isSwaggerUiHtmlRequest(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        String path = requestUri.startsWith(contextPath) ? requestUri.substring(contextPath.length()) : requestUri;
        return "/swagger-ui.html".equals(path);
    }

    private void addChannelHeader(io.swagger.v3.oas.models.Operation operation, String name, boolean required, String description) {
        List<Parameter> parameters = operation.getParameters();
        if (parameters == null) {
            parameters = new ArrayList<>();
            operation.setParameters(parameters);
        }
        boolean exists = parameters.stream()
                .anyMatch(parameter -> "header".equals(parameter.getIn()) && name.equals(parameter.getName()));
        if (!exists) {
            parameters.add(new Parameter()
                    .in("header")
                    .name(name)
                    .required(required)
                    .description(description)
                    .schema(new StringSchema().minLength(1).maxLength(16)
                            .pattern("^[A-Z0-9][A-Z0-9_-]{0,15}$")));
        }
    }

    private void addHeader(io.swagger.v3.oas.models.Operation operation, String name, boolean required, String description) {
        List<Parameter> parameters = operation.getParameters();
        if (parameters == null) {
            parameters = new ArrayList<>();
            operation.setParameters(parameters);
        }
        boolean exists = parameters.stream()
                .anyMatch(parameter -> "header".equals(parameter.getIn()) && name.equals(parameter.getName()));
        if (!exists) {
            parameters.add(new Parameter()
                    .in("header")
                    .name(name)
                    .required(required)
                    .description(description)
                    .schema(new StringSchema()));
        }
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "Value";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private String lowerFirst(String value) {
        if (value == null || value.isBlank()) {
            return "controller";
        }
        return value.substring(0, 1).toLowerCase(Locale.ROOT) + value.substring(1);
    }

    private record OnlineTransactionMetadata(String operationId, String name) {
    }
}
