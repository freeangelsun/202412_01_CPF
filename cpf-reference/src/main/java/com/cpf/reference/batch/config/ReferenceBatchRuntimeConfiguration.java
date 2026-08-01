package com.cpf.reference.batch.config;
import com.cpf.reference.batch.operation.SpringBatchEduBusinessConsumer;
import com.cpf.reference.batch.runtime.EduBatchScenarioWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
@Configuration(proxyBeanMethods=false)
@ConditionalOnProperty(name="cpf.reference.features.batch.enabled",havingValue="true",matchIfMissing=true)
public class ReferenceBatchRuntimeConfiguration {
 @Bean EduBatchScenarioWorker eduBatchScenarioWorker(JdbcTemplate jdbc,ObjectMapper json){return new EduBatchScenarioWorker(jdbc,json);}
 @Bean SpringBatchEduBusinessConsumer springBatchEduBusinessConsumer(ApplicationContext context,JobLauncher launcher){return new SpringBatchEduBusinessConsumer(context,launcher);}
}
