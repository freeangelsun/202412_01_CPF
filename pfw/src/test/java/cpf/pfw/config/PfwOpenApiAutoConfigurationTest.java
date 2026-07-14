package cpf.pfw.config;

import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;

class PfwOpenApiAutoConfigurationTest {

    private final PfwOpenApiAutoConfiguration configuration = new PfwOpenApiAutoConfiguration();

    @Test
    void operationIdCustomizerPreservesExplicitId() throws Exception {
        Operation operation = new Operation().operationId("explicitOperationId");
        HandlerMethod handlerMethod = handlerMethod("search", String.class);

        configuration.cpfOperationIdCustomizer().customize(operation, handlerMethod);

        assertThat(operation.getOperationId()).isEqualTo("explicitOperationId");
    }

    @Test
    void operationIdCustomizerGeneratesStableIdsForOverloadedMethods() throws Exception {
        Operation stringOperation = new Operation();
        Operation longOperation = new Operation();

        configuration.cpfOperationIdCustomizer().customize(
                stringOperation,
                handlerMethod("search", String.class));
        configuration.cpfOperationIdCustomizer().customize(
                longOperation,
                handlerMethod("search", Long.class));

        assertThat(stringOperation.getOperationId()).isEqualTo("sampleSearchUsingString");
        assertThat(longOperation.getOperationId()).isEqualTo("sampleSearchUsingLong");
        assertThat(stringOperation.getOperationId()).isNotEqualTo(longOperation.getOperationId());
    }

    @Test
    void transactionHeadersAreRequiredOnlyForDeclaredBusinessTransaction() throws Exception {
        Operation transactionOperation = new Operation();
        Operation operationQuery = new Operation();

        configuration.cpfTransactionHeaderOperationCustomizer().customize(
                transactionOperation,
                handlerMethod("execute", String.class));
        configuration.cpfTransactionHeaderOperationCustomizer().customize(
                operationQuery,
                handlerMethod("search", String.class));

        assertThat(transactionOperation.getParameters())
                .anySatisfy(parameter -> {
                    assertThat(parameter.getName()).isEqualTo("X-Transaction-Id");
                    assertThat(parameter.getRequired()).isTrue();
                });
        assertThat(operationQuery.getParameters())
                .extracting(io.swagger.v3.oas.models.parameters.Parameter::getName)
                .containsExactly("X-Trace-Id", "X-Correlation-Id")
                .doesNotContain("X-Transaction-Id");
    }

    @Test
    void standardErrorResponsesReferenceSharedSchema() throws Exception {
        Operation operation = new Operation();

        configuration.cpfStandardErrorResponseCustomizer().customize(
                operation,
                handlerMethod("search", String.class));

        assertThat(operation.getResponses().keySet())
                .contains("400", "401", "403", "404", "409", "429", "500", "503");
        assertThat(operation.getResponses().get("409").getContent().get("application/json").getSchema().get$ref())
                .isEqualTo("#/components/schemas/CpfErrorResponse");
        assertThat(configuration.cpfOpenAPI(new org.springframework.mock.env.MockEnvironment())
                .getComponents().getSchemas()).containsKey("CpfErrorResponse");
    }

    private HandlerMethod handlerMethod(String methodName, Class<?> parameterType) throws Exception {
        return new HandlerMethod(
                new SampleController(),
                SampleController.class.getDeclaredMethod(methodName, parameterType));
    }

    private static class SampleController {
        @CpfTransaction(id = "PFW01API0001", name = "OpenAPI 거래 헤더 테스트")
        @SuppressWarnings("unused")
        public void execute(String value) {
        }

        @SuppressWarnings("unused")
        public void search(String keyword) {
        }

        @SuppressWarnings("unused")
        public void search(Long id) {
        }
    }
}
