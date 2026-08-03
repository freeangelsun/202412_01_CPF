package com.cpf.starter.messaging.kafka.internal;

import com.cpf.core.api.broker.CpfBrokerConsumerControl;
import com.cpf.core.api.broker.CpfBrokerConsumerControlPort;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

/** Applies CPF provider-neutral consumer control to Spring Kafka listener containers. */
@Component
public final class CpfKafkaConsumerControlAdapter implements CpfBrokerConsumerControlPort {
    private final KafkaListenerEndpointRegistry registry;
    private final String groupId;

    public CpfKafkaConsumerControlAdapter(
            KafkaListenerEndpointRegistry registry,
            @Value("${cpf.messaging.kafka.consumer-control.group-id:${cpf.batch.worker.group-id:cpf-batch-worker}}")
            String groupId) {
        this.registry = registry;
        this.groupId = groupId;
    }

    @Override
    public void apply(CpfBrokerConsumerControl control) {
        List<MessageListenerContainer> containers = registry.getAllListenerContainers().stream()
                .filter(container -> groupId.equals(container.getGroupId()))
                .toList();
        if (containers.isEmpty()) {
            throw new IllegalStateException("No Kafka listener container is bound to group " + groupId);
        }
        for (MessageListenerContainer container : containers) {
            if (container instanceof ConcurrentMessageListenerContainer<?, ?> concurrent) {
                concurrent.setConcurrency(control.concurrency());
            }
            if (control.paused()) {
                if (!container.isPauseRequested()) {
                    container.pause();
                }
            } else if (container.isPauseRequested() || container.isContainerPaused()) {
                container.resume();
            }
        }
    }
}
