package com.cpf.starter.messaging.reliability;
import com.cpf.core.common.broker.*;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
@AutoConfiguration
@EnableConfigurationProperties(CpfMessagingReliabilityProperties.class)
public class CpfBrokerReliabilityAutoConfiguration {
 @Bean JdbcCpfBrokerReliabilityRepository cpfBrokerReliabilityRepository(@Qualifier("cpfJdbcTemplate") JdbcTemplate jdbc){return new JdbcCpfBrokerReliabilityRepository(jdbc);}
 @Bean CpfBrokerConsumerRuntimePolicy cpfBrokerConsumerRuntimePolicy(){return new CpfBrokerConsumerRuntimePolicy();}
 @Bean CpfBrokerConsumerWorker cpfBrokerConsumerWorker(JdbcCpfBrokerReliabilityRepository repo,CpfBrokerConsumerRuntimePolicy policy){return new CpfBrokerConsumerWorker(repo,repo,policy);}
 @Bean @ConditionalOnBean(CpfBrokerPublisher.class) CpfBrokerPublisherWorker cpfBrokerPublisherWorker(JdbcCpfBrokerReliabilityRepository repo,CpfBrokerPublisher publisher){return new CpfBrokerPublisherWorker(repo,publisher);}
 @Bean CpfBrokerReliabilityOperations cpfBrokerReliabilityOperations(JdbcCpfBrokerReliabilityRepository repo){return new CpfBrokerReliabilityOperations(repo);}
 @Bean @ConditionalOnMissingBean CpfBrokerClientRouter cpfBrokerClientRouter(ObjectProvider<CpfNamedBrokerClient> clients){return new CpfBrokerClientRouter(clients.orderedStream().toList());}
 @Bean SmartInitializingSingleton cpfBrokerSchemaVerifier(CpfMessagingReliabilityProperties p,DataSource dataSource){return ()->{p.validate();if(!p.isEnabled()||!p.isSchemaRequired())return;try(Connection c=dataSource.getConnection()){for(String t:List.of("cpf_broker_outbox","cpf_broker_inbox","cpf_broker_dlq")){try(var rs=c.getMetaData().getTables(c.getCatalog(),null,t,null)){if(!rs.next())throw new IllegalStateException("Missing CPF broker table: "+t);}}}catch(Exception ex){throw new IllegalStateException("CPF broker reliability schema verification failed",ex);}};}
 @Bean("cpfBrokerReliabilityHealthIndicator") HealthIndicator health(DataSource ds){return ()->{try(Connection c=ds.getConnection()){return c.isValid(3)?Health.up().build():Health.down().build();}catch(Exception ex){return Health.down(ex).build();}};}
}
