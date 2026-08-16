package com.cpf.starter.runtime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/** Public cpf-starter가 선택 Provider를 강제 로딩하지 않는지 검증합니다. */
class CpfStarterFootprintTest {
    @Test void optionalProvidersAreNotBaseClasses(){
        for(String name:new String[]{
                "org.apache.kafka.clients.producer.KafkaProducer",
                "com.rabbitmq.client.Connection",
                "com.ibm.mq.jms.MQConnectionFactory",
                "org.springframework.jms.core.JmsTemplate",
                "io.lettuce.core.RedisClient",
                "software.amazon.awssdk.services.s3.S3Client",
                "com.atomikos.icatch.jta.UserTransactionManager",
                "com.arjuna.ats.jta.TransactionManager"})
            assertThrows(ClassNotFoundException.class,()->Class.forName(name));
    }
}
