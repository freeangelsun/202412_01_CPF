package cpf.pfw.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
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
import java.util.List;

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
                        .url("/docs"));
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
}
