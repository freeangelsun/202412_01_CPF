package com.cpf.batch.control.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class BatRetentionExecutionServiceWiringTest {
    @Test
    void springSelectsTheProductionConstructor() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(BatRetentionOperations.class, () -> mock(BatRetentionOperations.class));
            context.registerBean(BatRetentionExecutionRepository.class,
                    () -> mock(BatRetentionExecutionRepository.class));
            context.register(BatRetentionExecutionService.class);
            context.refresh();

            assertThat(context.getBean(BatRetentionExecutionService.class)).isNotNull();
        }
    }
}
