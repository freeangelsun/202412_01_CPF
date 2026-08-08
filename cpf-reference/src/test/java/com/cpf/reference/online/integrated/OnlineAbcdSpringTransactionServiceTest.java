package com.cpf.reference.online.integrated;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OnlineAbcdSpringTransactionServiceTest {
    @Test void unknownResult는SpringTransaction을rollbackOnly로표시한다() {
        var repo = new OnlineAbcdReferenceFlow.InMemoryRepository();
        var remote = new OnlineAbcdReferenceFlow.ScenarioRemote();
        var c = new OnlineAbcdReferenceFlow.DomainC(remote);
        var b = new OnlineAbcdReferenceFlow.DomainB(c, repo);
        b.failAfterSave = true;
        var controller = new OnlineAbcdReferenceFlow.Controller(new OnlineAbcdReferenceFlow.DomainA(b));

        PlatformTransactionManager tm = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(tm.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
        var service = new OnlineAbcdSpringTransactionService(controller, tm);

        var result = service.execute(new OnlineAbcdReferenceFlow.Request("TX-SPRING", "K1", "P1", 1));
        assertEquals(OnlineAbcdReferenceFlow.Outcome.UNKNOWN, result.outcome());
        verify(status).setRollbackOnly();
        verify(tm).commit(status);
    }

    @Test void success는rollbackOnly없이commit경로를사용한다() {
        var repo = new OnlineAbcdReferenceFlow.InMemoryRepository();
        var remote = new OnlineAbcdReferenceFlow.ScenarioRemote();
        var c = new OnlineAbcdReferenceFlow.DomainC(remote);
        var b = new OnlineAbcdReferenceFlow.DomainB(c, repo);
        var controller = new OnlineAbcdReferenceFlow.Controller(new OnlineAbcdReferenceFlow.DomainA(b));

        PlatformTransactionManager tm = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(tm.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
        var service = new OnlineAbcdSpringTransactionService(controller, tm);

        var result = service.execute(new OnlineAbcdReferenceFlow.Request("TX-SPRING", "K2", "P2", 1));
        assertEquals(OnlineAbcdReferenceFlow.Outcome.SUCCESS, result.outcome());
        verify(status, never()).setRollbackOnly();
        verify(tm).commit(status);
    }
}
