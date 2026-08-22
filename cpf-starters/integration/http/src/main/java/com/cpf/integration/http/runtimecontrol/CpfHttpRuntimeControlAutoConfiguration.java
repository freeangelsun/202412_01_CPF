package com.cpf.integration.http.runtimecontrol;

import com.cpf.integration.fixedlength.api.CpfFixedLengthLayoutRegistry;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.integration.http.internal.CpfApiClientRuntimePolicy;
import com.cpf.integration.http.internal.CpfServiceEndpointRegistry;
import com.cpf.integration.http.internal.servicecall.CpfServiceRegistryRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * HTTP capability의 운영 중 변경값을 표준 Runtime Control Plane에 연결하는 자동 구성입니다.
 *
 * <p>API client 정책, 외부기관 endpoint, service route/circuit/maintenance 변경을
 * {@link CpfRuntimeChangeApplier} Bean으로 노출하며 실제 관련 runtime Bean이 존재할 때만 활성화됩니다.</p>
 */
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
