package com.cpf.starter.http.runtimecontrol;

import com.cpf.core.api.fixedlength.CpfFixedLengthLayoutRegistry;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.common.http.CpfApiClientRuntimePolicy;
import com.cpf.core.common.http.CpfServiceEndpointRegistry;
import com.cpf.core.common.servicecall.CpfServiceRegistryRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CpfHttpRuntimeControlAutoConfiguration {
    @Bean(name="cpfApiClientRuntimeApplier") @ConditionalOnBean(CpfApiClientRuntimePolicy.class)
    @ConditionalOnMissingBean(name="cpfApiClientRuntimeApplier")
    CpfRuntimeChangeApplier api(CpfApiClientRuntimePolicy policy){ return new CpfApiClientRuntimeApplier(policy); }

    @Bean(name="cpfExternalInstitutionRuntimeApplier") @ConditionalOnBean(CpfServiceEndpointRegistry.class)
    @ConditionalOnMissingBean(name="cpfExternalInstitutionRuntimeApplier")
    CpfRuntimeChangeApplier institution(CpfServiceEndpointRegistry registry, ObjectProvider<CpfFixedLengthLayoutRegistry> layouts){
        return new CpfExternalInstitutionRuntimeApplier(registry, layouts.getIfAvailable());
    }

    @Bean(name="cpfServiceRouteRuntimeApplier") @ConditionalOnBean(CpfServiceRegistryRepository.class)
    @ConditionalOnMissingBean(name="cpfServiceRouteRuntimeApplier")
    CpfRuntimeChangeApplier route(CpfServiceRegistryRepository repository){ return new CpfServiceRegistryRuntimeVerifierApplier("SERVICE_ROUTE", repository); }
    @Bean(name="cpfCircuitRuntimeApplier") @ConditionalOnBean(CpfServiceRegistryRepository.class)
    @ConditionalOnMissingBean(name="cpfCircuitRuntimeApplier")
    CpfRuntimeChangeApplier circuit(CpfServiceRegistryRepository repository){ return new CpfServiceRegistryRuntimeVerifierApplier("CIRCUIT", repository); }
    @Bean(name="cpfMaintenanceRuntimeApplier") @ConditionalOnBean(CpfServiceRegistryRepository.class)
    @ConditionalOnMissingBean(name="cpfMaintenanceRuntimeApplier")
    CpfRuntimeChangeApplier maintenance(CpfServiceRegistryRepository repository){ return new CpfServiceRegistryRuntimeVerifierApplier("MAINTENANCE", repository); }
}
