package com.cpf.bizadmin.auth.service;

import com.cpf.bizadmin.auth.repository.BzaAuthRepository;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository.BzaOperatorRow;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository.LoginOperationState;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BzaLoginTransactionServiceTest {
    private static final Instant REFRESH_EXPIRES_AT = Instant.parse("2026-07-29T00:00:00Z");
    private static final Instant RESULT_EXPIRES_AT = Instant.parse("2026-07-28T14:00:00Z");

    private final BzaAuthRepository repository = mock(BzaAuthRepository.class);
    private final BzaLoginTransactionService service = new BzaLoginTransactionService(repository);

    @Test
    void firstSuccessCommitsSingleDurableResult() {
        BzaOperatorRow operator = operator();
        when(repository.insertLoginOperation("op-1", 100L, "biz-admin", "hash-a")).thenReturn(true);
        when(repository.lockLoginOperation("op-1"))
                .thenReturn(Optional.of(state("op-1", "hash-a", "PROCESSING", null, null, null, null)));

        BzaLoginTransactionService.LoginCommitResult result = service.commitSuccess(command("op-1", "hash-a", operator));

        assertFalse(result.replayed());
        assertEquals("enc-access", result.resultAccessTokenEnc());
        assertEquals("enc-refresh", result.resultRefreshTokenEnc());
        verify(repository).markLoginSuccess(100L);
        verify(repository).insertLoginHistory(any());
        verify(repository).insertRefreshToken(any());
        verify(repository).markLoginOperationSuccess(
                "op-1", "enc-access", "enc-refresh", REFRESH_EXPIRES_AT, RESULT_EXPIRES_AT);
    }

    @Test
    void sameOperationAndSamePayloadReplaysOriginalResultWithoutAnotherSession() {
        BzaOperatorRow operator = operator();
        when(repository.insertLoginOperation("op-1", 100L, "biz-admin", "hash-a")).thenReturn(false);
        when(repository.lockLoginOperation("op-1"))
                .thenReturn(Optional.of(state(
                        "op-1", "hash-a", "SUCCESS", "stored-access", "stored-refresh",
                        REFRESH_EXPIRES_AT, Instant.now().plusSeconds(600))));

        BzaLoginTransactionService.LoginCommitResult result = service.commitSuccess(command("op-1", "hash-a", operator));

        assertTrue(result.replayed());
        assertEquals("stored-access", result.resultAccessTokenEnc());
        assertEquals("stored-refresh", result.resultRefreshTokenEnc());
        verify(repository, never()).markLoginSuccess(anyLong());
        verify(repository, never()).insertLoginHistory(any());
        verify(repository, never()).insertRefreshToken(any());
        verify(repository, never()).markLoginOperationSuccess(anyString(), anyString(), anyString(), any(), any());
    }

    @Test
    void sameOperationWithDifferentPayloadIsRejected() {
        BzaOperatorRow operator = operator();
        when(repository.insertLoginOperation("op-1", 100L, "biz-admin", "hash-b")).thenReturn(false);
        when(repository.lockLoginOperation("op-1"))
                .thenReturn(Optional.of(state("op-1", "hash-a", "SUCCESS", "a", "r",
                        REFRESH_EXPIRES_AT, Instant.now().plusSeconds(600))));

        assertThrows(ResponseStatusException.class,
                () -> service.commitSuccess(command("op-1", "hash-b", operator)));

        verify(repository, never()).insertRefreshToken(any());
    }

    @Test
    void expiredReplayResultIsRejectedInsteadOfRotatingRefreshToken() {
        BzaOperatorRow operator = operator();
        when(repository.insertLoginOperation("op-1", 100L, "biz-admin", "hash-a")).thenReturn(false);
        when(repository.lockLoginOperation("op-1"))
                .thenReturn(Optional.of(state("op-1", "hash-a", "SUCCESS", "a", "r",
                        REFRESH_EXPIRES_AT, Instant.EPOCH)));

        assertThrows(ResponseStatusException.class,
                () -> service.commitSuccess(command("op-1", "hash-a", operator)));

        verify(repository, never()).insertRefreshToken(any());
    }

    private LoginOperationState state(
            String operationId,
            String requestHash,
            String status,
            String accessToken,
            String refreshToken,
            Instant refreshExpiresAt,
            Instant resultExpiresAt) {
        return new LoginOperationState(
                operationId,
                100L,
                "biz-admin",
                requestHash,
                status,
                accessToken,
                refreshToken,
                refreshExpiresAt,
                resultExpiresAt,
                null,
                null);
    }

    private BzaLoginTransactionService.LoginSuccessCommand command(
            String operationId,
            String requestHash,
            BzaOperatorRow operator) {
        return new BzaLoginTransactionService.LoginSuccessCommand(
                operationId,
                requestHash,
                operator,
                "old-hash",
                null,
                "refresh-hash",
                REFRESH_EXPIRES_AT,
                "enc-access",
                "enc-refresh",
                RESULT_EXPIRES_AT,
                "127.0.0.1",
                "test",
                "tx-1",
                "BZA",
                "bzaAP01",
                "instance-1");
    }

    private BzaOperatorRow operator() {
        return new BzaOperatorRow(
                100L,
                "biz-admin",
                "업무 관리자",
                "password-hash",
                "BIZ_MANAGER",
                "ACTIVE",
                "Y",
                "N",
                0,
                "N",
                Instant.now().plusSeconds(86400),
                null,
                List.of("SETTING"),
                List.of("SETTING:READ"));
    }
}
