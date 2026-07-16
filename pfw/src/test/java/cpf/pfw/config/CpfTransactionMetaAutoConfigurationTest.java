package cpf.pfw.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import cpf.pfw.common.transaction.CpfTransactionMetaRepository;
import cpf.pfw.common.transaction.CpfTransactionMetaScanner;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** Actuator가 별도 handler mapping을 추가해도 거래 메타 scanner가 MVC mapping을 선택하는지 검증합니다. */
class CpfTransactionMetaAutoConfigurationTest {

    @Test
    void selectsMvcHandlerMappingWhenMultipleMappingsExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(
                    "requestMappingHandlerMapping",
                    RequestMappingHandlerMapping.class,
                    RequestMappingHandlerMapping::new);
            context.registerBean(
                    "controllerEndpointHandlerMapping",
                    RequestMappingHandlerMapping.class,
                    RequestMappingHandlerMapping::new);
            context.registerBean(
                    CpfTransactionMetaRepository.class,
                    () -> mock(CpfTransactionMetaRepository.class));
            context.register(CpfTransactionMetaAutoConfiguration.class);

            context.refresh();

            assertThat(context.getBean(CpfTransactionMetaScanner.class)).isNotNull();
        }
    }
}
