package com.cpf.batch.execution;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.batch.execution.context.CpfBatchContextCarrier;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.core.env.Environment;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
@AutoConfigureBefore(CpfBatchExecutionAutoConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(name="cpf.batch.remote.transport",havingValue="kafka")
@EnableConfigurationProperties(CpfBatchKafkaRemoteProperties.class)
@EnableKafka
public class CpfBatchKafkaRemoteConfiguration {
    private static final String WORKER_CONTAINER_FACTORY = "cpfBatchKafkaManualAckContainerFactory";

    @Bean CpfBatchContextCarrier cpfBatchContextCarrier(CpfExecutionIdGenerator executionIds){return new CpfBatchContextCarrier(executionIds);}
    @Bean CpfBatchRemoteCodec cpfBatchRemoteCodec(ObjectMapper mapper,CpfBatchKafkaRemoteProperties p,CpfBatchContextCarrier carrier){return new CpfBatchRemoteCodec(mapper,p,carrier);}
    @Bean CpfBatchRemoteMessageLedger cpfBatchRemoteMessageLedger(DataSource dataSource,CpfBatchKafkaRemoteProperties p,CpfVendorSqlCatalogProvider sqlCatalogProvider){return new JdbcCpfBatchRemoteMessageLedger(new JdbcTemplate(dataSource),Math.max(30,p.messageTtl().toSeconds()),sqlCatalogProvider);}
    @Bean("cpfBatchRequestTopic") String cpfBatchRequestTopic(CpfBatchKafkaRemoteProperties p){return p.requestTopic();}
    @Bean("cpfBatchWorkerGroupId") String cpfBatchWorkerGroupId(CpfBatchKafkaRemoteProperties p){return p.consumerGroup();}
    @Bean("cpfBatchManagerReplyTopic") String cpfBatchManagerReplyTopic(CpfBatchKafkaRemoteProperties p){return p.managerReplyTopic();}
    @Bean("cpfBatchManagerReplyGroup") String cpfBatchManagerReplyGroup(CpfBatchKafkaRemoteProperties p){return "cpf-batch-manager-"+p.managerInstanceId();}

    @Bean("cpfBatchKafkaManualAckContainerFactory")
    ConcurrentKafkaListenerContainerFactory<String,String> cpfBatchKafkaManualAckContainerFactory(
            ConsumerFactory<String,String> consumerFactory,KafkaTemplate<String,String> kafka,CpfBatchKafkaRemoteProperties p){
        ConcurrentKafkaListenerContainerFactory<String,String> factory=new ConcurrentKafkaListenerContainerFactory<>();factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        DeadLetterPublishingRecoverer recoverer=new DeadLetterPublishingRecoverer(kafka,(record,error)->new TopicPartition(record.topic()+".DLT",record.partition()));
        DefaultErrorHandler errorHandler=new DefaultErrorHandler(recoverer,new FixedBackOff(p.retryBackoffMillis(),p.maxDeliveryAttempts()-1L));
        errorHandler.setCommitRecovered(true);factory.setCommonErrorHandler(errorHandler);return factory;
    }

    /**
     * Applies the Worker admission limit to the optional Kafka consumer before its listener
     * container is created. Generic Worker execution admission remains transport-neutral.
     */
    @Bean
    @ConditionalOnExpression("'${cpf.batch.remote.kafka.role:ALL}'.equalsIgnoreCase('WORKER') || '${cpf.batch.remote.kafka.role:ALL}'.equalsIgnoreCase('ALL')")
    static BeanPostProcessor cpfBatchWorkerKafkaConcurrencyCustomizer(Environment environment) {
        int configured = environment.getProperty(
                "cpf.batch.worker.max-concurrency", Integer.class, 1);
        if (configured < 1 || configured > BatchRuntimePolicy.MAX_CONCURRENCY) {
            throw new IllegalArgumentException("Worker max concurrency is out of range");
        }
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (WORKER_CONTAINER_FACTORY.equals(beanName)
                        && bean instanceof ConcurrentKafkaListenerContainerFactory<?, ?> factory) {
                    factory.setConcurrency(configured);
                }
                return bean;
            }
        };
    }

    @Bean("cpfBatchRemoteRequests") MessageChannel cpfBatchRemoteRequests(KafkaTemplate<String,String> kafka,CpfBatchRemoteCodec codec,CpfBatchKafkaRemoteProperties p){return new CpfKafkaOutboundMessageChannel(kafka,codec,p.requestTopic(),null,p.sendTimeout());}
    @Bean("cpfBatchWorkerReplies") MessageChannel cpfBatchWorkerReplies(KafkaTemplate<String,String> kafka,CpfBatchRemoteCodec codec,CpfBatchKafkaRemoteProperties p){return new CpfKafkaOutboundMessageChannel(kafka,codec,null,p.replyTopicPrefix(),p.sendTimeout());}
    @Bean("cpfBatchRemoteReplies") PollableChannel cpfBatchRemoteReplies(CpfBatchKafkaRemoteProperties p){return new QueueChannel(p.replyQueueCapacity());}
    @Bean("cpfBatchWorkerRequests") CpfSynchronousWorkerChannel cpfBatchWorkerRequests(){return new CpfSynchronousWorkerChannel();}
    @Bean("cpfBatchChunkWorkerRequests") CpfSynchronousWorkerChannel cpfBatchChunkWorkerRequests(){return new CpfSynchronousWorkerChannel();}
    @Bean CpfBatchKafkaInboundBridge cpfBatchKafkaInboundBridge(CpfBatchRemoteCodec codec,@Qualifier("cpfBatchWorkerRequests")CpfSynchronousWorkerChannel steps,@Qualifier("cpfBatchChunkWorkerRequests")CpfSynchronousWorkerChannel chunks,@Qualifier("cpfBatchRemoteReplies")PollableChannel replies,CpfBatchRemoteMessageLedger ledger,CpfBatchKafkaRemoteProperties p){return new CpfBatchKafkaInboundBridge(codec,steps,chunks,replies,ledger,p.producerId());}
    @Bean @ConditionalOnExpression("'${cpf.batch.remote.kafka.role:ALL}'.equalsIgnoreCase('WORKER') || '${cpf.batch.remote.kafka.role:ALL}'.equalsIgnoreCase('ALL')") CpfBatchKafkaWorkerListener cpfBatchKafkaWorkerListener(CpfBatchKafkaInboundBridge bridge){return new CpfBatchKafkaWorkerListener(bridge);}
    @Bean @ConditionalOnExpression("'${cpf.batch.remote.kafka.role:ALL}'.equalsIgnoreCase('MANAGER') || '${cpf.batch.remote.kafka.role:ALL}'.equalsIgnoreCase('ALL')") CpfBatchKafkaManagerListener cpfBatchKafkaManagerListener(CpfBatchKafkaInboundBridge bridge){return new CpfBatchKafkaManagerListener(bridge);}
}
