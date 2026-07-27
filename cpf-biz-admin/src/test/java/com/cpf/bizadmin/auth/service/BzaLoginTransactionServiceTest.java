package com.cpf.bizadmin.auth.service;

import com.cpf.bizadmin.auth.repository.BzaAuthRepository;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository.BzaOperatorRow;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository.LoginOperationState;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BzaLoginTransactionServiceTest {
    private final BzaAuthRepository repository = mock(BzaAuthRepository.class);
    private final BzaLoginTransactionService service = new BzaLoginTransactionService(repository);

    @Test
    void firstSuccessCommitsAccountHistoryAndSingleRefreshSession() {
        BzaOperatorRow operator = operator();
        when(repository.insertLoginOperation("op-1", 100L, "biz-admin")).thenReturn(true);
        when(repository.lockLoginOperation("op-1"))
                .thenReturn(Optional.of(new LoginOperationState("op-1", 100L, "biz-admin", "PROCESSING")));

        service.commitSuccess(command("op-1", operator));

        verify(repository).markLoginSuccess(100L);
        verify(repository).insertLoginHistory(any());
        verify(repository).revokeRefreshTokensByLoginOperationId("op-1");
        verify(repository).insertRefreshToken(any());
        verify(repository).markLoginOperationSuccess("op-1");
    }

    @Test
    void existingNonSuccessOperationIsRejectedInsteadOfIssuingAnotherSession() {
        BzaOperatorRow operator = operator();
        when(repository.insertLoginOperation("op-1", 100L, "biz-admin")).thenReturn(false);
        when(repository.lockLoginOperation("op-1"))
                .thenReturn(Optional.of(new LoginOperationState("op-1", 100L, "biz-admin", "PROCESSING")));

        assertThrows(ResponseStatusException.class, () -> service.commitSuccess(command("op-1", operator)));

        verify(repository, never()).insertRefreshToken(any());
        verify(repository, never()).markLoginOperationSuccess(anyString());
    }

    @Test
    void responseLossRetryDoesNotDuplicateSuccessHistoryAndRotatesRefreshSession() {
        BzaOperatorRow operator = operator();
        when(repository.insertLoginOperation("op-1", 100L, "biz-admin")).thenReturn(false);
        when(repository.lockLoginOperation("op-1"))
                .thenReturn(Optional.of(new LoginOperationState("op-1", 100L, "biz-admin", "SUCCESS")));

        service.commitSuccess(command("op-1", operator));

        verify(repository, never()).markLoginSuccess(anyLong());
        verify(repository, never()).insertLoginHistory(any());
        verify(repository).revokeRefreshTokensByLoginOperationId("op-1");
        verify(repository).insertRefreshToken(any());
    }

    private BzaLoginTransactionService.LoginSuccessCommand command(String operationId, BzaOperatorRow operator) {
        return new BzaLoginTransactionService.LoginSuccessCommand(operationId, operator, "old-hash", null,
                "refresh-hash", Instant.now().plusSeconds(600), "127.0.0.1", "test", "tx-1",
                "BZA", "bzaAP01", "instance-1");
    }

    private BzaOperatorRow operator() {
        return new BzaOperatorRow(100L, "biz-admin", "업무 관리자", "password-hash", "BIZ_MANAGER",
                "ACTIVE", "Y", "N", 0, "N", Instant.now().plusSeconds(86400), null,
                List.of("SETTING"), List.of("SETTING:READ"));
    }
}
