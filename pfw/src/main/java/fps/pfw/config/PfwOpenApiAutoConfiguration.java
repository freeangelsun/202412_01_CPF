package fps.pfw.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * FPS 서비스 공통 OpenAPI 자동 설정입니다.
 *
 * <p>각 주제영역 애플리케이션은 PFW를 의존성으로 포함하면 Swagger UI, Scalar,
 * OpenAPI JSON/YAML 문서를 자동으로 사용할 수 있습니다. 또한 모든 API 문서에
 * FPS 거래 헤더를 공통 파라미터로 표시해 개발자가 헤더 누락 없이 테스트할 수 있게 합니다.</p>
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
public class PfwOpenApiAutoConfiguration {

    /**
     * 서비스별 OpenAPI 기본 정보를 구성합니다.
     *
     * @param environment 현재 애플리케이션 환경
     * @return OpenAPI 기본 문서 정보
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAPI fpsOpenAPI(Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", "fps-service").toUpperCase();
        return new OpenAPI()
                .info(new Info()
                        .title("FPS " + applicationName + " API")
                        .version(environment.getProperty("fps.openapi.version", "v1"))
                        .description("금융 서비스 프레임워크 표준 거래 API 문서입니다. "
                                + "업무 거래ID, 필수 거래 헤더, 표준 예외, 캐시/트랜잭션 샘플을 함께 확인합니다.")
                        .contact(new Contact()
                                .name("FPS Framework Team")
                                .email("fps-framework@example.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("FPS 개발 표준 가이드")
                        .url("/docs"));
    }

    /**
     * 전체 API 그룹입니다.
     *
     * @return 전체 API 그룹
     */
    @Bean
    public GroupedOpenApi fpsAllApiGroup() {
        return GroupedOpenApi.builder()
                .group("00-ALL 전체 API")
                .pathsToMatch("/**")
                .build();
    }

    /**
     * MBR 기본회원관리 API 그룹입니다.
     *
     * @return MBR-BSE 그룹
     */
    @Bean
    public GroupedOpenApi fpsMbrBseApiGroup() {
        return GroupedOpenApi.builder()
                .group("MBR-BSE 회원기본관리")
                .pathsToMatch("/mbr/**")
                .build();
    }

    /**
     * ACC 기본업무 API 그룹입니다.
     *
     * @return ACC-BSE 그룹
     */
    @Bean
    public GroupedOpenApi fpsAccBseApiGroup() {
        return GroupedOpenApi.builder()
                .group("ACC-BSE 계좌기본/회원연계")
                .pathsToMatch("/members/**")
                .build();
    }

    /**
     * ACC 테스트/공통관리 API 그룹입니다.
     *
     * @return ACC-TST 그룹
     */
    @Bean
    public GroupedOpenApi fpsAccTstApiGroup() {
        return GroupedOpenApi.builder()
                .group("ACC-TST 테스트/공통관리")
                .pathsToMatch("/acc/**", "/fps/codes/**")
                .build();
    }

    /**
     * XYZ 교육용 API 그룹입니다.
     *
     * @return XYZ-EDU 그룹
     */
    @Bean
    public GroupedOpenApi fpsXyzEduApiGroup() {
        return GroupedOpenApi.builder()
                .group("XYZ-EDU 교육용 샘플")
                .pathsToMatch("/xyz/edu/**")
                .build();
    }

    /**
     * 모든 API Operation에 FPS 거래 헤더를 표시합니다.
     *
     * @return Operation 커스터마이저
     */
    @Bean
    public OperationCustomizer fpsTransactionHeaderOperationCustomizer() {
        return (operation, handlerMethod) -> {
            addHeader(operation, "X-Transaction-Id", false, "글로벌 거래ID. 없으면 PFW가 자동 생성합니다.");
            addHeader(operation, "X-Request-Type", true, "요청 구분. 예: INQUIRY, CREATE, UPDATE, DELETE, PROCESS");
            addHeader(operation, "X-Original-Channel-Code", true, "최초 전송 채널 코드. 예: WEB, MOB, BATCH");
            addHeader(operation, "X-Channel-Code", true, "현재 전송 채널 코드. 예: WEB, MOB, API");
            addHeader(operation, "X-Member-No", false, "회원번호. 있으면 거래 로그 검색 키로 적재합니다.");
            addHeader(operation, "X-Customer-No", false, "고객번호. 있으면 거래 로그 검색 키로 적재합니다.");
            addHeader(operation, "X-User-Id", false, "거래 수행자 ID. 로그의 실행 사용자로 사용합니다.");
            addHeader(operation, "X-Client-IP", false, "클라이언트 IP. 없으면 서버가 요청 IP를 추정합니다.");
            return operation;
        };
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
