package cpf.pfw.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import cpf.pfw.common.logging.CpfTransaction;
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
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
public class PfwOpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI cpfOpenAPI(Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", "cpf-service").toUpperCase();
        return new OpenAPI()
                .info(new Info()
                        .title("CPF " + applicationName + " API")
                        .version(environment.getProperty("cpf.openapi.version", "v1"))
                        .description("CoreFlow Platform Framework 표준 API 문서입니다.")
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
    public GroupedOpenApi cpfMbrBseApiGroup() {
        return GroupedOpenApi.builder()
                .group("MBR-BSE Member")
                .pathsToMatch("/mbr/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cpfAccBseApiGroup() {
        return GroupedOpenApi.builder()
                .group("ACC-BSE Account")
                .pathsToMatch("/accounts/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cpfAccTstApiGroup() {
        return GroupedOpenApi.builder()
                .group("ACC-TST Common Sample")
                .pathsToMatch("/acc/**", "/cpf/codes/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cpfXyzEduApiGroup() {
        return GroupedOpenApi.builder()
                .group("XYZ-EDU Education")
                .pathsToMatch("/xyz/edu/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cpfAdmOprApiGroup() {
        return GroupedOpenApi.builder()
                .group("ADM-OPR Administration")
                .pathsToMatch("/adm/api/**")
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
            CpfTransaction transaction = handlerMethod.getMethodAnnotation(CpfTransaction.class);
            if (transaction == null) {
                transaction = handlerMethod.getBeanType().getAnnotation(CpfTransaction.class);
            }
            if (transaction == null) {
                addHeader(operation, "X-Trace-Id", false, "선택 분산 추적 ID입니다. 없으면 CPF가 생성할 수 있습니다.");
                addHeader(operation, "X-Correlation-Id", false, "외부 시스템과 함께 보는 선택 상관관계 ID입니다.");
                return operation;
            }
            addHeader(
                    operation,
                    "X-Transaction-Id",
                    true,
                    "트랜잭션 글로벌 ID입니다. yyyyMMddHHmmssSSS + moduleId 3자리 + wasId 7자리 + sequence 7자리 형식입니다.");
            addHeader(operation, "X-Trace-Id", false, "분산 추적 ID입니다. 없으면 CPF가 생성합니다.");
            addHeader(operation, "X-Span-Id", false, "현재 호출 span ID입니다. CPF가 응답 헤더로 반환합니다.");
            addHeader(operation, "X-Parent-Span-Id", false, "상위 호출 span ID입니다. 서비스 간 호출 전파에 사용합니다.");
            addHeader(operation, "X-Api-Version", false, "호출 API 버전입니다. 예: v1.");
            addHeader(operation, "X-Client-App-Id", false, "클라이언트 앱 또는 제휴 시스템 ID입니다.");
            addHeader(operation, "X-Client-Version", false, "클라이언트 앱 또는 SDK 버전입니다.");
            addHeader(operation, "X-Caller-Service", false, "호출한 내부 서비스 또는 배치명입니다.");
            addHeader(operation, "X-Caller-Instance-Id", false, "호출한 인스턴스, WAS, pod 식별자입니다.");
            addHeader(operation, "X-Correlation-Id", false, "외부 시스템과 함께 보는 상관관계 ID입니다.");
            addHeader(operation, "X-Idempotency-Key", false, "중복 처리 방지를 위한 멱등 키입니다.");
            addHeader(operation, "X-Locale", false, "클라이언트 언어/국가 코드입니다. 예: ko-KR.");
            addHeader(operation, "X-Timezone", false, "클라이언트 시간대입니다. 예: Asia/Seoul.");
            addHeader(operation, "X-Request-Type", true, "요청 유형입니다. 예: INQUIRY, CREATE, UPDATE, DELETE, PROCESS.");
            addHeader(operation, "X-Original-Channel-Code", true, "최초 유입 채널 코드입니다. 예: WEB, MOB, BATCH.");
            addHeader(operation, "X-Channel-Code", true, "현재 처리 채널 코드입니다. 예: WEB, MOB, API.");
            addHeader(operation, "X-Member-No", false, "감사와 추적에 사용하는 회원 번호입니다.");
            addHeader(operation, "X-Customer-No", false, "감사와 추적에 사용하는 고객 번호입니다.");
            addHeader(operation, "X-User-Id", false, "사용자 또는 운영자 ID입니다.");
            addHeader(operation, "X-Screen-Id", false, "화면 또는 메뉴 식별자입니다.");
            addHeader(operation, "X-Device-Id", false, "단말 또는 디바이스 식별자입니다.");
            addHeader(operation, "X-Client-Request-Time", false, "클라이언트가 요청을 만든 시간입니다.");
            addHeader(operation, "X-Client-IP", false, "클라이언트 IP 주소입니다.");
            addHeader(operation, "X-Reserved-Field-1", false, "업무 확장 예약 필드 1입니다.");
            addHeader(operation, "X-Reserved-Field-2", false, "업무 확장 예약 필드 2입니다.");
            addHeader(operation, "X-Reserved-Field-3", false, "업무 확장 예약 필드 3입니다.");
            addHeader(operation, "X-Reserved-Field-4", false, "업무 확장 예약 필드 4입니다.");
            addHeader(operation, "X-Reserved-Field-5", false, "업무 확장 예약 필드 5입니다.");
            return operation;
        };
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
            addErrorResponse(responses, "429", "호출 한도 초과");
            addErrorResponse(responses, "500", "서버 내부 처리 오류");
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
}
