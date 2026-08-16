package com.cpf.education.batch.support.config;
import com.cpf.education.batch.support.operation.SpringBatchEduBusinessConsumer;
import com.cpf.education.batch.support.runtime.EduBatchScenarioWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
@Configuration(proxyBeanMethods=false)
@ConditionalOnProperty(name="cpf.education.features.batch.enabled",havingValue="true",matchIfMissing=true)
/** EducationBatchRuntimeConfiguration 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationBatchRuntimeConfiguration {
 @Bean EduBatchScenarioWorker eduBatchScenarioWorker(JdbcTemplate jdbc,ObjectMapper json){return new EduBatchScenarioWorker(jdbc,json);}
 @Bean SpringBatchEduBusinessConsumer springBatchEduBusinessConsumer(ApplicationContext context,JobOperator operator){return new SpringBatchEduBusinessConsumer(context,operator);}
}
