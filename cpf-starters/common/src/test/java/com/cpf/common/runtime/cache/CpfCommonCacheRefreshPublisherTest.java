package com.cpf.common.runtime.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CpfCommonCacheRefreshPublisherTest {
    @AfterEach void cleanup(){ if(TransactionSynchronizationManager.isSynchronizationActive()) TransactionSynchronizationManager.clearSynchronization(); }

    @Test void localRefreshRunsOnlyAfterCommit() {
        var repo=mock(CpfCommonCacheRefreshEventRepository.class); var refresher=mock(CpfCommonCacheRefresher.class);
        when(repo.insertEvent(anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(11L);
        var publisher=new CpfCommonCacheRefreshPublisher(repo,refresher);
        TransactionSynchronizationManager.initSynchronization();
        assertThat(publisher.publishRequired("codeCache","UPSERT","code_id=1","tester")).isEqualTo(11L);
        verifyNoInteractions(refresher);
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
        verify(refresher).refresh("codeCache");
    }

    @Test void rollbackNeverRefreshesLocalCache() {
        var repo=mock(CpfCommonCacheRefreshEventRepository.class); var refresher=mock(CpfCommonCacheRefresher.class);
        when(repo.insertEvent(anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(12L);
        var publisher=new CpfCommonCacheRefreshPublisher(repo,refresher);
        TransactionSynchronizationManager.initSynchronization(); publisher.publishRequired("messageCache","UPSERT","message_id=1","tester");
        TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(1));
        verifyNoInteractions(refresher);
    }
}
