package cpf.pfw.config;

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

    private HandlerMethod handlerMethod(String methodName, Class<?> parameterType) throws Exception {
        return new HandlerMethod(
                new SampleController(),
                SampleController.class.getDeclaredMethod(methodName, parameterType));
    }

    private static class SampleController {
        @SuppressWarnings("unused")
        public void search(String keyword) {
        }

        @SuppressWarnings("unused")
        public void search(Long id) {
        }
    }
}
