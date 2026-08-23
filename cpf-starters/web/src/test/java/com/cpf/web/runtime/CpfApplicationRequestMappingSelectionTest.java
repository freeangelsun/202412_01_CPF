package com.cpf.web.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.web.internal.openapi.CpfOpenApiAutoConfiguration;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** 업무 API Consumer가 Actuator 관리 매핑과 혼동하지 않는 Bean 선택 계약입니다. */
class CpfApplicationRequestMappingSelectionTest {
    @Test
    void applicationMappingConsumersSelectCanonicalMvcHandlerMapping() {
        assertApplicationMappingQualifier(method(CpfOpenApiAutoConfiguration.class, "cpfOpenApiOperations"));
        assertApplicationMappingQualifier(method(CpfControllerPolicyAutoConfiguration.class, "cpfOperationCatalogBootstrap"));
    }

    private static Method method(Class<?> owner, String name) {
        return Arrays.stream(owner.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static void assertApplicationMappingQualifier(Method factoryMethod) {
        Parameter mapping = Arrays.stream(factoryMethod.getParameters())
                .filter(parameter -> parameter.getType().equals(RequestMappingHandlerMapping.class))
                .findFirst()
                .orElseThrow();
        Qualifier qualifier = mapping.getAnnotation(Qualifier.class);
        assertThat(qualifier).isNotNull();
        assertThat(qualifier.value()).isEqualTo("requestMappingHandlerMapping");
    }
}
