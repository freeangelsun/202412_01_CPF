package com.cpf.capability;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.starter.runtime.CpfCapabilityUsageAspect;
import com.cpf.starter.runtime.CpfRuntimeCapabilityDescriptor;
import com.cpf.starter.runtime.CpfRuntimeCapabilityInventory;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;

/** Capability 관측 AOP가 Spring infrastructure를 업무 Consumer로 proxy하지 않는 계약입니다. */
class CpfCapabilityUsageAspectPointcutTest {
    @Test
    void descriptorAwareAdvisorExcludesInfrastructureButMatchesProxySafeConsumer() throws Exception {
        CpfRuntimeCapabilityInventory inventory = new CpfRuntimeCapabilityInventory();
        inventory.register(descriptor(BusinessCapabilityConsumer.class.getName()));
        CpfCapabilityUsageAspect advisor = new CpfCapabilityUsageAspect(inventory);

        Method infrastructure = FinalInfrastructurePostProcessor.class
                .getMethod("postProcessBeforeInitialization", Object.class, String.class);
        Method consumer = BusinessCapabilityConsumer.class.getMethod("invoke");
        Method unknown = UnownedConsumer.class.getMethod("invoke");

        assertThat(advisor.matches(infrastructure, FinalInfrastructurePostProcessor.class)).isFalse();
        assertThat(advisor.matches(consumer, BusinessCapabilityConsumer.class)).isTrue();
        assertThat(advisor.matches(unknown, UnownedConsumer.class)).isFalse();
    }

    private static CpfRuntimeCapabilityDescriptor descriptor(String packageBase) {
        return new CpfRuntimeCapabilityDescriptor(
                "test.capability", "cpf-starter-test", "TEST", "cpf", "cpf.test", "test",
                true, "capability", "RUNTIME", false, true, true, "capability",
                List.of("OPERATIONS"),
                new CpfRuntimeCapabilityDescriptor.Support(true, true, true, true, true,
                        true, true, true, true, true),
                Map.of("packageBase", packageBase));
    }

    /** final은 infrastructure 불변성 계약이며 AOP 편의를 위해 제거하면 안 됩니다. */
    static final class FinalInfrastructurePostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            return bean;
        }
    }

    static class BusinessCapabilityConsumer {
        public String invoke() { return "ok"; }
    }

    static class UnownedConsumer {
        public String invoke() { return "ignored"; }
    }
}
