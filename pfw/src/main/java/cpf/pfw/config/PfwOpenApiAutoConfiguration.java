package cpf.pfw.config;

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

@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
public class PfwOpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI fpsOpenAPI(Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", "cpf-service").toUpperCase();
        return new OpenAPI()
                .info(new Info()
                        .title("CPF " + applicationName + " API")
                        .version(environment.getProperty("cpf.openapi.version", "v1"))
                        .description("CoreFlow Platform Framework standard API documentation.")
                        .contact(new Contact()
                                .name("CPF Framework Team")
                                .email("cpf-framework@example.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("CPF framework documentation")
                        .url("/docs"));
    }

    @Bean
    public GroupedOpenApi fpsAllApiGroup() {
        return GroupedOpenApi.builder()
                .group("00-ALL API")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public GroupedOpenApi fpsMbrBseApiGroup() {
        return GroupedOpenApi.builder()
                .group("MBR-BSE Member")
                .pathsToMatch("/mbr/**")
                .build();
    }

    @Bean
    public GroupedOpenApi fpsAccBseApiGroup() {
        return GroupedOpenApi.builder()
                .group("ACC-BSE Account")
                .pathsToMatch("/members/**")
                .build();
    }

    @Bean
    public GroupedOpenApi fpsAccTstApiGroup() {
        return GroupedOpenApi.builder()
                .group("ACC-TST Common Sample")
                .pathsToMatch("/acc/**", "/cpf/codes/**")
                .build();
    }

    @Bean
    public GroupedOpenApi fpsXyzEduApiGroup() {
        return GroupedOpenApi.builder()
                .group("XYZ-EDU Education")
                .pathsToMatch("/xyz/edu/**")
                .build();
    }

    @Bean
    public GroupedOpenApi fpsAdmOprApiGroup() {
        return GroupedOpenApi.builder()
                .group("ADM-OPR Administration")
                .pathsToMatch("/adm/api/**")
                .build();
    }

    @Bean
    public OperationCustomizer fpsTransactionHeaderOperationCustomizer() {
        return (operation, handlerMethod) -> {
            addHeader(operation, "X-Transaction-Id", false, "Global transaction id. CPF generates one when absent.");
            addHeader(operation, "X-Request-Type", true, "Request type. Example: INQUIRY, CREATE, UPDATE, DELETE, PROCESS.");
            addHeader(operation, "X-Original-Channel-Code", true, "Original channel code. Example: WEB, MOB, BATCH.");
            addHeader(operation, "X-Channel-Code", true, "Current channel code. Example: WEB, MOB, API.");
            addHeader(operation, "X-Member-No", false, "Member number for audit and tracing.");
            addHeader(operation, "X-Customer-No", false, "Customer number for audit and tracing.");
            addHeader(operation, "X-User-Id", false, "User or operator id.");
            addHeader(operation, "X-Client-IP", false, "Client IP address.");
            addHeader(operation, "X-Reserved-Field-1", false, "Reserved field 1.");
            addHeader(operation, "X-Reserved-Field-2", false, "Reserved field 2.");
            addHeader(operation, "X-Reserved-Field-3", false, "Reserved field 3.");
            addHeader(operation, "X-Reserved-Field-4", false, "Reserved field 4.");
            addHeader(operation, "X-Reserved-Field-5", false, "Reserved field 5.");
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