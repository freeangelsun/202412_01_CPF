package com.cpf.data.persistence.jdbc.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.data.persistence.sql.CpfConnectionPoolRuntimeController;
import com.cpf.data.persistence.sql.CpfReadRoutingRuntimePolicy;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CpfPersistenceRuntimeControlAutoConfiguration {
    @Bean
    @ConditionalOnProperty(
            prefix = "cpf.db.connection-pool.runtime-control",
            name = "enabled",
            havingValue = "true")
    @ConditionalOnMissingBean
    CpfConnectionPoolRuntimeController connectionPoolRuntimeController(
            @Qualifier("cpfPrimaryDataSource") DataSource primary,
            @Qualifier("cpfReplicaDataSource") ObjectProvider<DataSource> replicaProvider) {
        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(primary);
        DataSource replica = replicaProvider.getIfAvailable();
        if (replica != null) {
            dataSources.add(replica);
        }
        return new CpfConnectionPoolRuntimeController(dataSources);
    }

    @Bean(name = "cpfConnectionPoolRuntimeApplier")
    @ConditionalOnBean(CpfConnectionPoolRuntimeController.class)
    @ConditionalOnMissingBean(name = "cpfConnectionPoolRuntimeApplier")
    CpfRuntimeChangeApplier connectionPoolRuntimeApplier(
            CpfConnectionPoolRuntimeController controller) {
        return new CpfConnectionPoolRuntimeApplier(controller);
    }

    @Bean(name = "cpfDbReadRoutingRuntimeApplier")
    @ConditionalOnProperty(
            prefix = "cpf.db.read-routing",
            name = "enabled",
            havingValue = "true")
    @ConditionalOnBean(CpfReadRoutingRuntimePolicy.class)
    @ConditionalOnMissingBean(name = "cpfDbReadRoutingRuntimeApplier")
    CpfRuntimeChangeApplier dbReadRoutingRuntimeApplier(
            CpfReadRoutingRuntimePolicy policy) {
        return new CpfDbReadRoutingRuntimeApplier(policy);
    }
}
