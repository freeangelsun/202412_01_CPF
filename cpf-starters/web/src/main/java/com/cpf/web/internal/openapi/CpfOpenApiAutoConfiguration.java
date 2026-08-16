package com.cpf.web.internal.openapi;

import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.core.api.version.CpfPlatformVersion;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.api.openapi.CpfOpenApiOperations;
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
        String applicationName = environment.getProperty("spring.application.name", "cpf-service").toUpperCase();
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
            OnlineTransactionMetadata standard = findOnlineTransaction(handlerMethod);
            if (standard == null) {
                addHeader(operation, CpfHttpHeaderNames.TRACEPARENT, false, "W3C 분산 추적 traceparent입니다.");
                addHeader(operation, CpfHttpHeaderNames.CORRELATION_ID, false, "외부 시스템과 함께 보는 선택 상관관계 ID입니다.");
                return operation;
            }
            addHeader(operation, CpfHttpHeaderNames.TRANSACTION_ID, true, "CPF 거래 상관관계 ID입니다.");
            addHeader(operation, CpfHttpHeaderNames.STANDARD_EXECUTION_ID, false,
                    "호출 대상 표준 실행 ID입니다. 대상 값 " + standard.id() + "와 일치해야 합니다.");
            addHeader(operation, CpfHttpHeaderNames.PROTOCOL_VERSION, false, "CPF 호출 규격 버전입니다.");
            addHeader(operation, CpfHttpHeaderNames.TRACEPARENT, false, "W3C 분산 추적 traceparent입니다.");
            addHeader(operation, CpfHttpHeaderNames.CORRELATION_ID, false, "외부 시스템과 함께 보는 상관관계 ID입니다.");
            addHeader(operation, CpfHttpHeaderNames.IDEMPOTENCY_KEY, false, "중복 처리 방지를 위한 멱등 키입니다.");
            addHeader(operation, CpfHttpHeaderNames.API_VERSION, false, "호출 API 버전입니다.");
            addHeader(operation, CpfHttpHeaderNames.ORIGINAL_CHANNEL_CODE, true, "최초 유입 채널 코드입니다.");
            addHeader(operation, CpfHttpHeaderNames.CHANNEL_CODE, true, "현재 처리 채널 코드입니다.");
            addHeader(operation, CpfHttpHeaderNames.REQUEST_TYPE, true, "요청 유형입니다.");
            addHeader(operation, CpfHttpHeaderNames.USER_ID, false, "사용자 ID입니다.");
            addHeader(operation, CpfHttpHeaderNames.OPERATOR_ID, false, "운영자 ID입니다.");
            addHeader(operation, CpfHttpHeaderNames.SCREEN_ID, false, "화면 또는 메뉴 식별자입니다.");
            addHeader(operation, CpfHttpHeaderNames.DEVICE_ID, false, "단말 또는 디바이스 식별자입니다.");
            addHeader(operation, CpfHttpHeaderNames.LOCALE, false, "클라이언트 Locale입니다.");
            addHeader(operation, CpfHttpHeaderNames.CLIENT_TIMEZONE, false, "클라이언트 시간대입니다.");
            addHeader(operation, CpfHttpHeaderNames.CLIENT_IP, false, "클라이언트 IP입니다.");
            return operation;
        };
    }

    private OnlineTransactionMetadata findOnlineTransaction(HandlerMethod handlerMethod) {
        CpfOnlineTransaction standard = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), CpfOnlineTransaction.class);
        if (standard == null) standard = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), CpfOnlineTransaction.class);
        return standard == null ? null : new OnlineTransactionMetadata(standard.id(), standard.name());
    }


    @Bean
    @ConditionalOnMissingBean(CpfOpenApiOperations.class)
    public CpfOpenApiOperations cpfOpenApiOperations(CpfOpenApiProperties properties, RequestMappingHandlerMapping mappings) {
        return new DefaultCpfOpenApiOperations(properties, mappings, Clock.systemUTC());
    }

    /**
     * 명시적 operationId가 없는 API에도 재현 가능한 식별자를 부여합니다.
     *
     * <p>개발자가 업무 의미가 담긴 operationId를 선언하면 그 값을 우선 사용합니다. 선언하지 않은 경우에는
     * Controller, 메서드, 파라미터 타입을 조합하므로 메서드 오버로드도 서로 다른 ID를 가집니다.</p>
     */
    @Bean
    public OperationCustomizer cpfOperationIdCustomizer() {
        return (operation, handlerMethod) -> {
            if (operation.getOperationId() == null || operation.getOperationId().isBlank()) {
                operation.setOperationId(generatedOperationId(handlerMethod));
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

    private record OnlineTransactionMetadata(String id, String name) {
    }
}
