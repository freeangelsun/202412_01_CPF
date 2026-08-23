package com.cpf.messaging.kafka.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.messaging.api.CpfBrokerConsumerControl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;

class CpfKafkaConsumerControlAdapterTest {
    private static final String GROUP_ID = "cpf-batch-worker";

    @Test
    void appliesConcurrencyAndPauseOnlyToConfiguredGroup() {
        KafkaListenerEndpointRegistry registry = mock(KafkaListenerEndpointRegistry.class);
        @SuppressWarnings("unchecked")
        ConcurrentMessageListenerContainer<Object, Object> worker =
                mock(ConcurrentMessageListenerContainer.class);
        MessageListenerContainer unrelated = mock(MessageListenerContainer.class);
        when(worker.getGroupId()).thenReturn(GROUP_ID);
        when(unrelated.getGroupId()).thenReturn("unrelated-group");
        when(registry.getAllListenerContainers()).thenReturn(List.of(worker, unrelated));

        new CpfKafkaConsumerControlAdapter(registry, GROUP_ID)
                .apply(new CpfBrokerConsumerControl(true, 4, 7));

        verify(worker).setConcurrency(4);
        verify(worker).pause();
        verify(unrelated, never()).pause();
        verify(unrelated, never()).resume();
    }

    @Test
    void resumesConfiguredGroupWhenPauseWasRequested() {
        KafkaListenerEndpointRegistry registry = mock(KafkaListenerEndpointRegistry.class);
        MessageListenerContainer worker = mock(MessageListenerContainer.class);
        when(worker.getGroupId()).thenReturn(GROUP_ID);
        when(worker.isPauseRequested()).thenReturn(true);
        when(registry.getAllListenerContainers()).thenReturn(List.of(worker));

        new CpfKafkaConsumerControlAdapter(registry, GROUP_ID)
                .apply(new CpfBrokerConsumerControl(false, 2, 3));

        verify(worker).resume();
        verify(worker, never()).pause();
    }

    @Test
    void missingConfiguredGroupFailsClosed() {
        KafkaListenerEndpointRegistry registry = mock(KafkaListenerEndpointRegistry.class);
        MessageListenerContainer unrelated = mock(MessageListenerContainer.class);
        when(unrelated.getGroupId()).thenReturn("unrelated-group");
        when(registry.getAllListenerContainers()).thenReturn(List.of(unrelated));

        assertThatThrownBy(() -> new CpfKafkaConsumerControlAdapter(registry, GROUP_ID)
                .apply(new CpfBrokerConsumerControl(false, 1, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(GROUP_ID);
        verify(unrelated, never()).pause();
        verify(unrelated, never()).resume();
    }
}
