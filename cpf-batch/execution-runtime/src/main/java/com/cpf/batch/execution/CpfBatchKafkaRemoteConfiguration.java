package com.cpf.batch.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;

@AutoConfiguration
@AutoConfigureBefore(CpfBatchExecutionAutoConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(name="cpf.batch.remote.transport", havingValue="kafka")
@EnableConfigurationProperties(CpfBatchKafkaRemoteProperties.class)
@EnableKafka
public class CpfBatchKafkaRemoteConfiguration {
    @Bean CpfBatchRemoteCodec cpfBatchRemoteCodec(ObjectMapper mapper) { return new CpfBatchRemoteCodec(mapper); }

    @Bean("cpfBatchRemoteRequests")
    MessageChannel cpfBatchRemoteRequests(
            KafkaTemplate<String, String> kafka,
            CpfBatchRemoteCodec codec,
            CpfBatchKafkaRemoteProperties properties) {
        return new CpfKafkaOutboundMessageChannel(kafka, codec, properties.requestTopic(), properties.sendTimeout());
    }

    @Bean("cpfBatchWorkerReplies")
    MessageChannel cpfBatchWorkerReplies(
            KafkaTemplate<String, String> kafka,
            CpfBatchRemoteCodec codec,
            CpfBatchKafkaRemoteProperties properties) {
        return new CpfKafkaOutboundMessageChannel(kafka, codec, properties.replyTopic(), properties.sendTimeout());
    }

    @Bean("cpfBatchRemoteReplies") PollableChannel cpfBatchRemoteReplies() { return new QueueChannel(); }
    @Bean("cpfBatchWorkerRequests") MessageChannel cpfBatchWorkerRequests() { return new DirectChannel(); }
    @Bean("cpfBatchChunkWorkerRequests") MessageChannel cpfBatchChunkWorkerRequests() { return new DirectChannel(); }

    @Bean
    CpfBatchKafkaInboundBridge cpfBatchKafkaInboundBridge(
            CpfBatchRemoteCodec codec,
            @Qualifier("cpfBatchWorkerRequests") MessageChannel stepRequests,
            @Qualifier("cpfBatchChunkWorkerRequests") MessageChannel chunkRequests,
            @Qualifier("cpfBatchRemoteReplies") PollableChannel replies) {
        return new CpfBatchKafkaInboundBridge(codec, stepRequests, chunkRequests, replies);
    }
    @Bean
    @ConditionalOnExpression("'${cpf.batch.remote.kafka.role:ALL}'.equalsIgnoreCase('WORKER') || '${cpf.batch.remote.kafka.role:ALL}'.equalsIgnoreCase('ALL')")
    CpfBatchKafkaWorkerListener cpfBatchKafkaWorkerListener(CpfBatchKafkaInboundBridge bridge) {
        return new CpfBatchKafkaWorkerListener(bridge);
    }

    @Bean
    @ConditionalOnExpression("'${cpf.batch.remote.kafka.role:ALL}'.equalsIgnoreCase('MANAGER') || '${cpf.batch.remote.kafka.role:ALL}'.equalsIgnoreCase('ALL')")
    CpfBatchKafkaManagerListener cpfBatchKafkaManagerListener(CpfBatchKafkaInboundBridge bridge) {
        return new CpfBatchKafkaManagerListener(bridge);
    }

}
