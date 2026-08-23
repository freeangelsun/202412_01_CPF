package com.cpf.admin.opr.service;

import com.cpf.common.spi.CpfCommonCacheChangePublisher;
import com.cpf.messaging.common.api.CmnMessageConsumer;
import com.cpf.messaging.common.api.CmnMessagePublisher;
import com.cpf.platform.operations.observability.api.logging.CpfDynamicLogLevelOperations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmDynamicLogLevelBroadcastServiceTest {

    @Test
    void deletePublishesDurableEventThroughCommonPublicOutOfBandContract() {
        CpfCommonCacheChangePublisher cachePublisher = mock(CpfCommonCacheChangePublisher.class);
        AdmDynamicLogLevelBroadcastService service = new AdmDynamicLogLevelBroadcastService(
                mock(AdmDynamicLogLevelRuleStore.class),
                mock(CpfDynamicLogLevelOperations.class),
                emptyProvider(),
                emptyProvider(),
                provider(cachePublisher));

        service.publishDelete("RULE-17", "approver-a");

        verify(cachePublisher).publishOutOfBand(
                "dynamicLogLevelRule", "DELETE", "RULE-17", "approver-a");
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    private static <T> ObjectProvider<T> emptyProvider() {
        return provider(null);
    }
}
