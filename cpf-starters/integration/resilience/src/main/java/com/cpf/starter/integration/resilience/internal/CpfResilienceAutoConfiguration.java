package com.cpf.starter.integration.resilience.internal;

import com.cpf.core.api.resilience.CpfResilienceExecutor;
import com.cpf.core.api.resilience.CpfResiliencePolicyOperations;
import com.cpf.core.spi.resilience.CpfResilienceAuditSink;
import com.cpf.core.spi.resilience.CpfResilienceFailureClassifier;
import com.cpf.core.spi.resilience.CpfResiliencePolicyStore;
import java.time.Clock;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfiguration
@ConditionalOnProperty(prefix="cpf.integration.resilience",name="enabled",havingValue="true")
public class CpfResilienceAutoConfiguration {
    @Bean @ConditionalOnMissingBean Clock cpfResilienceClock(){return Clock.systemUTC();}
    @Bean CpfResiliencePolicyStore cpfResiliencePolicyStore(DataSource ds,PlatformTransactionManager tx){return new JdbcCpfResiliencePolicyStore(new JdbcTemplate(ds),new TransactionTemplate(tx));}
    @Bean CpfResilienceAuditSink cpfResilienceAuditSink(DataSource ds){return new JdbcCpfResilienceAuditSink(new JdbcTemplate(ds));}
    @Bean @ConditionalOnMissingBean CpfResilienceFailureClassifier cpfResilienceFailureClassifier(){return new CpfDefaultFailureClassifier();}
    @Bean CpfResilienceExecutor cpfResilienceExecutor(CpfResiliencePolicyStore store,CpfResilienceFailureClassifier classifier,CpfResilienceAuditSink audit,Clock clock){return new CpfResilienceEngine(store,classifier,audit,clock,Executors.newVirtualThreadPerTaskExecutor());}
    @Bean CpfResiliencePolicyOperations cpfResiliencePolicyOperations(CpfResiliencePolicyStore store,CpfResilienceAuditSink audit,Clock clock,PlatformTransactionManager tx){return new CpfResiliencePolicyCommandService(store,audit,clock,new CpfSpringResilienceTransactionRunner(new TransactionTemplate(tx)));}
}
