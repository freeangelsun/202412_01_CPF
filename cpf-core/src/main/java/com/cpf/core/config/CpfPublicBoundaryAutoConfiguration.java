package com.cpf.core.config;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.core.api.filetransfer.CpfFileTransferClient;
import com.cpf.core.api.gateway.CpfGatewayRouteProvider;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.cpf.core.common.broker.CpfBrokerClientAdapter;
import com.cpf.core.common.broker.CpfBrokerOutboxPort;
import com.cpf.core.common.filetransfer.CpfFileTransferClientAdapter;
import com.cpf.core.common.filetransfer.CpfFileTransferEngine;
import com.cpf.core.api.execution.CpfExecutionCatalogPort;
import com.cpf.core.common.gateway.CpfGatewayRouteProviderAdapter;
import com.cpf.core.common.servicecall.CpfServiceCallEngine;
import com.cpf.core.common.servicecall.CpfServiceCallerAdapter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Generated Domain/외부 Runtime 공개 API와 Core 내부 구현 사이 adapter를 조립합니다. */
@AutoConfiguration(after = CpfServiceCallAutoConfiguration.class)
public class CpfPublicBoundaryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfVendorSqlCatalogProvider.class)
    CpfVendorSqlCatalogProvider cpfVendorSqlCatalogProvider(Environment environment) {
        return moduleCode ->
                com.cpf.core.common.database.CpfVendorSqlCatalog.create(environment, moduleCode);
    }

    @Bean @ConditionalOnBean(CpfBrokerOutboxPort.class) @ConditionalOnMissingBean(CpfBrokerClient.class)
    CpfBrokerClient cpfBrokerClient(CpfBrokerOutboxPort outbox){ return new CpfBrokerClientAdapter(outbox); }

    @Bean @ConditionalOnBean(CpfFileTransferEngine.class) @ConditionalOnMissingBean(CpfFileTransferClient.class)
    CpfFileTransferClient cpfFileTransferClient(CpfFileTransferEngine engine){ return new CpfFileTransferClientAdapter(engine); }

    @Bean @ConditionalOnBean(CpfExecutionCatalogPort.class) @ConditionalOnMissingBean(CpfGatewayRouteProvider.class)
    CpfGatewayRouteProvider cpfGatewayRouteProvider(CpfExecutionCatalogPort catalog){ return new CpfGatewayRouteProviderAdapter(catalog); }

    @Bean @ConditionalOnBean(CpfServiceCallEngine.class) @ConditionalOnMissingBean(CpfServiceCaller.class)
    CpfServiceCaller cpfServiceCaller(CpfServiceCallEngine engine){ return new CpfServiceCallerAdapter(engine); }
}
