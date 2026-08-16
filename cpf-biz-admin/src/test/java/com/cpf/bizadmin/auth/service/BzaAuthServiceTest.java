package com.cpf.bizadmin.auth.service;

import com.cpf.bizadmin.auth.dto.BzaLoginHistoryResponse;
import com.cpf.bizadmin.auth.dto.BzaSessionRevokeResponse;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository.BzaOperatorRow;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository.RefreshTokenRow;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository.RefreshTokenWrite;
import com.cpf.bizadmin.audit.service.BzaBusinessAuditService;
import com.cpf.security.common.crypto.CmnCryptoService;
import com.cpf.security.common.token.CmnJwtCreateRequest;
import com.cpf.security.common.token.CmnJwtService;
import com.cpf.security.common.token.CmnJwtValidationResult;
import com.cpf.security.api.password.CpfPasswordService;
import com.cpf.security.api.password.CpfPasswordVerification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BzaAuthServiceTest {

    private static final String RAW_REFRESH_TOKEN = "raw-bza-refresh-token-value";
    private static final String REFRESH_TOKEN_HASH = "hash-bza-refresh-token-value";

    private final CmnJwtService jwtService = mock(CmnJwtService.class);
    private final CmnCryptoService cryptoService = mock(CmnCryptoService.class);
    private final CpfPasswordService passwordHashingPort = mock(CpfPasswordService.class);
    private final BzaAuthRepository authRepository = mock(BzaAuthRepository.class);
    private final BzaBusinessAuditService auditService = mock(BzaBusinessAuditService.class);
    private final BzaLoginTransactionService loginTransactionService = mock(BzaLoginTransactionService.class);
    private final BzaAuthService service = new BzaAuthService(
            jwtService,
            cryptoService,
            passwordHashingPort,
            authRepository,
            auditService,
            loginTransactionService,
            "bza-test-secret-must-be-at-least-32-characters",
            600,
            7200,
            "BZA",
            "bzaAP01");

    @Test
    void loginStoresRefreshTokenHashAndSuccessHistory() {
        // 로그인 성공 시 refresh token 원문은 응답으로만 전달하고 DB에는 hash만 저장해야 합니다.
        BzaOperatorRow operator = operator("Y", "N", 0);
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator));
        when(passwordHashingPort.verify(any(char[].class), org.mockito.ArgumentMatchers.eq("password-hash")))
                .thenReturn(new CpfPasswordVerification(true, false));
        when(jwtService.createHs256Token(any(CmnJwtCreateRequest.class))).thenReturn("access-token");
        when(cryptoService.secureRandomToken(48)).thenReturn(RAW_REFRESH_TOKEN);
        when(cryptoService.sha256Base64Url(RAW_REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_HASH);
        when(cryptoService.sha256Hex("password")).thenReturn("password-request-hash");
        when(cryptoService.hmacSha256Hex(
                "BZA_LOGIN|biz-admin|password-request-hash",
                "bza-test-secret-must-be-at-least-32-characters"))
                .thenReturn("login-request-hash");
        when(cryptoService.aesGcmEncrypt(
                "access-token",
                "bza-test-secret-must-be-at-least-32-characters:BZA_LOGIN_RESULT"))
                .thenReturn("access-token-enc");
        when(cryptoService.aesGcmEncrypt(
                RAW_REFRESH_TOKEN,
                "bza-test-secret-must-be-at-least-32-characters:BZA_LOGIN_RESULT"))
                .thenReturn("refresh-token-enc");
        when(loginTransactionService.commitSuccess(
                any(BzaLoginTransactionService.LoginSuccessCommand.class)))
                .thenAnswer(invocation -> {
                    BzaLoginTransactionService.LoginSuccessCommand command =
                            invocation.getArgument(0);
                    return new BzaLoginTransactionService.LoginCommitResult(
                            command.resultAccessTokenEnc(),
                            command.resultRefreshTokenEnc(),
                            command.refreshExpireAt(),
                            false);
                });
        when(cryptoService.aesGcmDecrypt(
                "access-token-enc",
                "bza-test-secret-must-be-at-least-32-characters:BZA_LOGIN_RESULT"))
                .thenReturn("access-token");
        when(cryptoService.aesGcmDecrypt(
                "refresh-token-enc",
                "bza-test-secret-must-be-at-least-32-characters:BZA_LOGIN_RESULT"))
                .thenReturn(RAW_REFRESH_TOKEN);

        BzaAuthService.LoginResult result = service.login(
                new BzaAuthService.LoginRequest("biz-admin", "password", "login-op-1"),
                "127.0.0.1",
                "unit-test");

        verify(loginTransactionService).commitSuccess(any(BzaLoginTransactionService.LoginSuccessCommand.class));
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo(RAW_REFRESH_TOKEN);
    }

    @Test
    void loginFailureIncreasesFailCountAndDoesNotStoreToken() {
        // 비밀번호 실패 시 실패 횟수와 로그인 실패 이력만 남기고 refresh token은 만들지 않습니다.
        BzaOperatorRow operator = operator("Y", "N", 1);
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator));
        when(passwordHashingPort.verify(any(char[].class), org.mockito.ArgumentMatchers.eq("password-hash")))
                .thenReturn(CpfPasswordVerification.rejected());

        assertThatThrownBy(() -> service.login(
                new BzaAuthService.LoginRequest("biz-admin", "wrong", "login-op-2"),
                "127.0.0.1",
                "unit-test"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");

        verify(loginTransactionService).recordFailure(any(BzaLoginTransactionService.LoginFailureCommand.class));
        verify(loginTransactionService, never()).commitSuccess(any(BzaLoginTransactionService.LoginSuccessCommand.class));
        verify(authRepository, never()).insertRefreshToken(any(RefreshTokenWrite.class));
    }

    @Test
    void refreshRejectsTokenConsumedByConcurrentRequest() {
        // 조회 이후 다른 요청이 token을 먼저 폐기한 경우 조건부 UPDATE 0건으로 replay를 차단합니다.
        when(cryptoService.sha256Base64Url(RAW_REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_HASH);
        when(authRepository.findRefreshToken(REFRESH_TOKEN_HASH)).thenReturn(Optional.of(new RefreshTokenRow(
                REFRESH_TOKEN_HASH,
                100L,
                "biz-admin",
                "BZA",
                Instant.now().plusSeconds(600),
                false,
                "transaction-global-id")));
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator("Y", "N", 0)));
        when(authRepository.revokeRefreshToken(REFRESH_TOKEN_HASH)).thenReturn(0);

        assertThatThrownBy(() -> service.refresh(new BzaAuthService.RefreshRequest(RAW_REFRESH_TOKEN)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401")
                .hasMessageContaining("이미 사용");

        verify(authRepository, never()).insertRefreshToken(any(RefreshTokenWrite.class));
        verify(cryptoService, never()).secureRandomToken(48);
    }

    @Test
    void loginHistoryRequiresServerSideUserReadPermission() {
        BzaOperatorRow operator = new BzaOperatorRow(
                100L,
                "biz-admin",
                "업무 관리자",
                "password-hash",
                "BZA_MANAGER",
                "ACTIVE",
                "Y",
                "N",
                0,
                "N",
                Instant.now().plusSeconds(86400),
                null,
                List.of("AUTHORIZATION"),
                List.of("AUTHORIZATION:READ"));
        when(jwtService.validateHs256Token(
                "access-token",
                "bza-test-secret-must-be-at-least-32-characters",
                "CPF-BZA",
                "CPF-BZA"))
                .thenReturn(new CmnJwtValidationResult(
                        true,
                        "검증 성공",
                        "100",
                        "CPF-BZA",
                        "CPF-BZA",
                        Instant.now().plusSeconds(600),
                        Map.of("loginId", "biz-admin", "loginDomain", "BZA")));
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator));
        when(authRepository.findLoginHistories(500)).thenReturn(List.of(Map.of("loginResult", "SUCCESS")));

        List<BzaLoginHistoryResponse> result = service.loginHistories("Bearer access-token", 1000);

        assertThat(result).hasSize(1);
        verify(authRepository).findLoginHistories(500);
    }

    @Test
    void revokeSessionUsesTokenOwnerAndWritesReasonAudit() {
        BzaOperatorRow operator = operator("Y", "N", 0);
        when(jwtService.validateHs256Token(
                "access-token",
                "bza-test-secret-must-be-at-least-32-characters",
                "CPF-BZA",
                "CPF-BZA"))
                .thenReturn(new CmnJwtValidationResult(
                        true,
                        "검증 성공",
                        "100",
                        "CPF-BZA",
                        "CPF-BZA",
                        Instant.now().plusSeconds(600),
                        Map.of("loginId", "biz-admin", "loginDomain", "BZA")));
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator));
        when(authRepository.revokeRefreshSession(77L, 100L, "biz-admin")).thenReturn(1);

        BzaSessionRevokeResponse result =
                service.revokeSession("Bearer access-token", 77L, "분실 단말 세션 폐기");

        assertThat(result.sessionId()).isEqualTo(77L);
        assertThat(result.revoked()).isTrue();
        verify(auditService).record(
                "biz-admin",
                "SESSION_REVOKE",
                "bza_refresh_token",
                "77",
                "분실 단말 세션 폐기",
                null,
                Map.of("revokedYn", "Y"));
    }

    private BzaOperatorRow operator(String useYn, String lockYn, int failCount) {
        return new BzaOperatorRow(
                100L,
                "biz-admin",
                "업무 관리자",
                "password-hash",
                "BIZ_MANAGER",
                "ACTIVE",
                useYn,
                lockYn,
                failCount,
                "N",
                Instant.now().plusSeconds(86400),
                null,
                List.of("SETTING"),
                List.of("SETTING:READ"));
    }
}
