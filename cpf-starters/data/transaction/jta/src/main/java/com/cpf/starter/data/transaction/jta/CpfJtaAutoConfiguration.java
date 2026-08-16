package com.cpf.starter.data.transaction.jta;

import com.cpf.core.api.transaction.CpfXaTransactionManager;
import com.cpf.core.api.transaction.CpfXaRecoveryResourceProvider;
import java.util.List;
import jakarta.transaction.TransactionManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.ApplicationRunner;

/** JTA is opt-in. Managed WAS TM wins; Narayana is created only in explicit standalone mode. */
@AutoConfiguration
@EnableConfigurationProperties(CpfJtaProperties.class)
@ConditionalOnProperty(prefix="cpf.data.transaction.jta", name="enabled", havingValue="true")
public class CpfJtaAutoConfiguration {
    @Bean @ConditionalOnBean(JdbcTemplate.class) @ConditionalOnMissingBean(CpfXaRecoveryStore.class)
    CpfXaRecoveryStore cpfXaRecoveryStore(JdbcTemplate jdbc) { return new JdbcCpfXaRecoveryStore(jdbc); }
    @Bean
    @ConditionalOnMissingBean(TransactionManager.class)
    @ConditionalOnProperty(prefix="cpf.data.transaction.jta", name="standalone", havingValue="true")
    TransactionManager cpfStandaloneTransactionManager() {
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }
    @Bean
    @ConditionalOnClass(name="com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule")
    @ConditionalOnProperty(prefix="cpf.data.transaction.jta", name="standalone", havingValue="true")
    CpfNarayanaRecoveryRegistrar cpfNarayanaRecoveryRegistrar(List<CpfXaRecoveryResourceProvider> providers) {
        return new CpfNarayanaRecoveryRegistrar(providers);
    }

    @Bean
    @ConditionalOnProperty(prefix="cpf.data.transaction.jta", name="startup-recovery", havingValue="true", matchIfMissing=true)
    @ConditionalOnClass(name="com.arjuna.ats.arjuna.recovery.RecoveryManager")
    ApplicationRunner cpfXaStartupRecoveryScan() { return args -> com.arjuna.ats.arjuna.recovery.RecoveryManager.manager().scan(); }

    @Bean
    @ConditionalOnBean({TransactionManager.class, CpfXaRecoveryStore.class})
    @ConditionalOnMissingBean(CpfXaTransactionManager.class)
    CpfXaTransactionManager cpfXaTransactionManager(TransactionManager tm, CpfXaRecoveryStore store) {
        return new CpfJtaTransactionManagerAdapter(tm, store);
    }
}
