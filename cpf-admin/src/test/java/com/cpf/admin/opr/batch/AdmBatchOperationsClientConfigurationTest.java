package com.cpf.admin.opr.batch;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * ADM이 BAT 소유 DB를 직접 구성하지 않고 공개 Owner Port의 Remote Adapter를 선택하는지 검증합니다.
 */
class AdmBatchOperationsClientConfigurationTest {

    @Test
    void createsRemoteOwnerAdapterWithoutBatDataSource() {
        AdmBatchOperationsClientConfiguration configuration =
                new AdmBatchOperationsClientConfiguration();

        CpfBatchOperationsPort port = configuration.remoteCpfBatchOperationsPort(
                mock(CpfServiceCaller.class),
                WebClient.builder(),
                mock(AdmAuthenticatedOperatorContext.class),
                "adm-test-01");

        assertThat(port).isInstanceOf(RemoteCpfBatchOperationsAdapter.class);
    }
}
