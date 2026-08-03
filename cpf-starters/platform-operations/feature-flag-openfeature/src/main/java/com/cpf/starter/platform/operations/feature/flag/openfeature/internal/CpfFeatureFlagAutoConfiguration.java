package com.cpf.starter.platform.operations.feature.flag.openfeature.internal;
import com.cpf.core.api.featureflag.CpfFeatureFlagOperations;import com.cpf.core.spi.featureflag.*;import java.time.*;import javax.sql.DataSource;import org.springframework.boot.autoconfigure.AutoConfiguration;import org.springframework.boot.autoconfigure.condition.*;import org.springframework.context.annotation.Bean;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.transaction.PlatformTransactionManager;import org.springframework.transaction.support.TransactionTemplate;
@AutoConfiguration
@ConditionalOnProperty(prefix="cpf.platform-operations.feature-flag",name="enabled",havingValue="true")
public class CpfFeatureFlagAutoConfiguration{
 @Bean @ConditionalOnMissingBean Clock cpfFeatureFlagClock(){return Clock.systemUTC();}
 @Bean @ConditionalOnMissingBean CpfFeatureFlagProvider cpfOpenFeatureProvider(){return new CpfOpenFeatureProviderAdapter("cpf",0);}
 @Bean CpfFeatureFlagStateStore cpfFeatureFlagStateStore(DataSource ds,PlatformTransactionManager tx){return new JdbcCpfFeatureFlagStateStore(new JdbcTemplate(ds),new TransactionTemplate(tx));}
 @Bean CpfFeatureFlagAuditSink cpfFeatureFlagAuditSink(DataSource ds){return new JdbcCpfFeatureFlagAuditSink(new JdbcTemplate(ds));}
 @Bean CpfFeatureFlagOperations cpfFeatureFlagOperations(CpfFeatureFlagProvider p,CpfFeatureFlagStateStore s,CpfFeatureFlagAuditSink a,Clock c,PlatformTransactionManager tx){return new CpfFeatureFlagRuntime(p,s,a,c,Duration.ofSeconds(30),new CpfSpringFeatureFlagTransactionRunner(new TransactionTemplate(tx)));}
}
