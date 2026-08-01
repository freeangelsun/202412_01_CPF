package com.cpf.reference.edu.runtime.configuration;

import com.cpf.reference.edu.runtime.application.*;
import com.cpf.reference.edu.runtime.consumer.*;
import com.cpf.reference.edu.runtime.consumer.file.FileEduBusinessConsumer;
import com.cpf.reference.edu.runtime.consumer.http.HttpEduBusinessConsumer;
import com.cpf.reference.edu.runtime.consumer.jdbc.*;
import com.cpf.reference.edu.runtime.consumer.outbox.OutboxEduBusinessConsumer;
import com.cpf.reference.edu.runtime.consumer.process.ProcessEduBusinessConsumer;
import com.cpf.reference.edu.runtime.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.List;

/** Mandatory REF EDU runtime. Optional feature packages register contributors/consumers independently. */
@Configuration(proxyBeanMethods=false)
public class EduRuntimeConfiguration {
    @Bean EduCapabilityRegistry eduCapabilityRegistry(List<EduCapabilityContributor> contributors){
        return new EduCapabilityRegistry(contributors);
    }
    @Bean EduOperationRepository eduOperationRepository(JdbcTemplate jdbc,ObjectMapper json,
            @Value("${cpf.edu.repository:jdbc}") String mode,
            @Value("${cpf.edu.file-store:build/cpf-edu-store}") String fileStore){
        return "file".equalsIgnoreCase(mode)?new FileEduOperationRepository(Path.of(fileStore)):new JdbcEduOperationRepository(jdbc,json);
    }
    @Bean JdbcEduBusinessConsumer jdbcEduBusinessConsumer(JdbcTemplate jdbc,ObjectMapper json){return new JdbcEduBusinessConsumer(jdbc,json);}
    @Bean JdbcQueryEduBusinessConsumer jdbcQueryEduBusinessConsumer(JdbcEduBusinessConsumer commandConsumer){return new JdbcQueryEduBusinessConsumer(commandConsumer);}
    @Bean FileEduBusinessConsumer fileEduBusinessConsumer(ObjectMapper json,@Value("${cpf.edu.business-file-root:build/cpf-edu-business-files}") String root){return new FileEduBusinessConsumer(Path.of(root),json);}
    @Bean HttpEduBusinessConsumer httpEduBusinessConsumer(ObjectMapper json,Environment environment){return new HttpEduBusinessConsumer(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),json,environment);}
    @Bean ProcessEduBusinessConsumer processEduBusinessConsumer(ObjectMapper json,@Value("${cpf.repository-root:.}") String repositoryRoot){return new ProcessEduBusinessConsumer(Path.of(repositoryRoot),json);}
    @Bean OutboxEduBusinessConsumer outboxEduBusinessConsumer(){return new OutboxEduBusinessConsumer();}
    @Bean EduBusinessConsumerRegistry eduBusinessConsumerRegistry(List<EduBusinessConsumer> consumers){return new EduBusinessConsumerRegistry(consumers);}
    @Bean EduExecutionService eduExecutionService(EduCapabilityRegistry registry,EduOperationRepository repository,EduBusinessConsumerRegistry consumers,
            @Value("${cpf.instance-id:reference-1}") String instanceId){return new EduExecutionService(registry,repository,consumers,Clock.systemUTC(),instanceId);}
}
