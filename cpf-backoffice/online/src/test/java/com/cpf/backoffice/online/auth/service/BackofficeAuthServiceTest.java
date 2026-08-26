package com.cpf.backoffice.online.auth.service;

import com.cpf.backoffice.online.auth.dto.BackofficeLoginHistoryResponse;
import com.cpf.backoffice.online.auth.dto.BackofficeSessionRevokeResponse;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository.BackofficeOperatorRow;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository.RefreshTokenRow;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository.RefreshTokenWrite;
import com.cpf.backoffice.online.audit.service.BackofficeBusinessAuditService;
import com.cpf.security.common.crypto.CmnCryptoService;
import com.cpf.security.common.token.CmnJwtCreateRequest;
import com.cpf.security.common.token.CmnJwtService;
import com.cpf.security.common.token.CmnJwtValidationResult;
import com.cpf.security.api.password.CpfPasswordEncoder;
import com.cpf.testkit.context.CpfContextTestSupport;
import com.cpf.testkit.context.CpfTestContextRuntime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
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
class BackofficeAuthServiceTest {

    private static final String RAW_REFRESH_TOKEN = "raw-backoffice-refresh-token-value";
    private static final String REFRESH_TOKEN_HASH = "hash-backoffice-refresh-token-value";
    private static CpfTestContextRuntime contextRuntime;

    private final CpfContextTestSupport contexts =
            new CpfContextTestSupport("MBW-AUTH", LocalDate.of(2026, 8, 22));

    private final CmnJwtService jwtService = mock(CmnJwtService.class);
    private final CmnCryptoService cryptoService = mock(CmnCryptoService.class);
    private final CpfPasswordEncoder passwordHashingPort = mock(CpfPasswordEncoder.class);
    private final BackofficeAuthRepository authRepository = mock(BackofficeAuthRepository.class);
    private final BackofficeBusinessAuditService auditService = mock(BackofficeBusinessAuditService.class);
    private final BackofficeLoginTransactionService loginTransactionService = mock(BackofficeLoginTransactionService.class);
    private final BackofficeAuthService service = new BackofficeAuthService(
            jwtService,
            cryptoService,
            passwordHashingPort,
            authRepository,
            auditService,
            loginTransactionService,
            "backoffice-test-secret-must-be-at-least-32-characters",
            600,
            7200,
            new MockEnvironment()
                    .withProperty("cpf.system-code", "MBW")
                    .withProperty("spring.application.name", "MBW_AP01"));

    @BeforeAll
    static void installContextRuntime() {
        contextRuntime = CpfTestContextRuntime.install();
    }

    @AfterAll
    static void closeContextRuntime() {
        contextRuntime.close();
    }

    @AfterEach
    void assertContextClear() {
        contexts.assertClear();
    }

    @Test
    void loginStoresRefreshTokenHashAndSuccessHistory() throws Exception {
        // 로그인 성공 시 refresh token 원문은 응답으로만 전달하고 DB에는 hash만 저장해야 합니다.
        BackofficeOperatorRow operator = operator("Y", "N", 0);
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator));
        when(passwordHashingPort.matches(any(char[].class), org.mockito.ArgumentMatchers.eq("password-hash")))
                .thenReturn(true);
        when(jwtService.createHs256Token(any(CmnJwtCreateRequest.class))).thenReturn("access-token");
        when(cryptoService.secureRandomToken(48)).thenReturn(RAW_REFRESH_TOKEN);
        when(cryptoService.sha256Base64Url(RAW_REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_HASH);
        when(cryptoService.sha256Hex("password")).thenReturn("password-request-hash");
        when(cryptoService.hmacSha256Hex(
                "MBW_LOGIN|biz-admin|password-request-hash",
                "backoffice-test-secret-must-be-at-least-32-characters"))
                .thenReturn("login-request-hash");
        when(cryptoService.aesGcmEncrypt(
                "access-token",
                "backoffice-test-secret-must-be-at-least-32-characters:MBW_LOGIN_RESULT"))
                .thenReturn("access-token-enc");
        when(cryptoService.aesGcmEncrypt(
                RAW_REFRESH_TOKEN,
                "backoffice-test-secret-must-be-at-least-32-characters:MBW_LOGIN_RESULT"))
                .thenReturn("refresh-token-enc");
        when(loginTransactionService.commitSuccess(
                any(BackofficeLoginTransactionService.LoginSuccessCommand.class)))
                .thenAnswer(invocation -> {
                    BackofficeLoginTransactionService.LoginSuccessCommand command =
                            invocation.getArgument(0);
                    return new BackofficeLoginTransactionService.LoginCommitResult(
                            command.resultAccessTokenEnc(),
                            command.resultRefreshTokenEnc(),
                            command.refreshExpireAt(),
                            false);
                });
        when(cryptoService.aesGcmDecrypt(
                "access-token-enc",
                "backoffice-test-secret-must-be-at-least-32-characters:MBW_LOGIN_RESULT"))
                .thenReturn("access-token");
        when(cryptoService.aesGcmDecrypt(
                "refresh-token-enc",
                "backoffice-test-secret-must-be-at-least-32-characters:MBW_LOGIN_RESULT"))
                .thenReturn(RAW_REFRESH_TOKEN);

        BackofficeAuthService.LoginResult result;
        try (AutoCloseable _ = contexts.bindRoot("correlation-login-success", null, "biz-admin")) {
            result = service.login(
                    new BackofficeAuthService.LoginRequest("biz-admin", "password", "login-op-1"),
                    "127.0.0.1",
                    "unit-test");
        }

        verify(loginTransactionService).commitSuccess(any(BackofficeLoginTransactionService.LoginSuccessCommand.class));
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo(RAW_REFRESH_TOKEN);
    }

    @Test
    void loginFailureIncreasesFailCountAndDoesNotStoreToken() throws Exception {
        // 비밀번호 실패 시 실패 횟수와 로그인 실패 이력만 남기고 refresh token은 만들지 않습니다.
        BackofficeOperatorRow operator = operator("Y", "N", 1);
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator));
        when(passwordHashingPort.matches(any(char[].class), org.mockito.ArgumentMatchers.eq("password-hash")))
                .thenReturn(false);

        try (AutoCloseable _ = contexts.bindRoot("correlation-login-failure", null, "biz-admin")) {
            assertThatThrownBy(() -> service.login(
                    new BackofficeAuthService.LoginRequest("biz-admin", "wrong", "login-op-2"),
                    "127.0.0.1",
                    "unit-test"))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("401");
        }

        verify(loginTransactionService).recordFailure(any(BackofficeLoginTransactionService.LoginFailureCommand.class));
        verify(loginTransactionService, never()).commitSuccess(any(BackofficeLoginTransactionService.LoginSuccessCommand.class));
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
                "MBW",
                Instant.now().plusSeconds(600),
                false,
                "transaction-global-id")));
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator("Y", "N", 0)));
        when(authRepository.revokeRefreshToken(REFRESH_TOKEN_HASH)).thenReturn(0);

        assertThatThrownBy(() -> service.refresh(new BackofficeAuthService.RefreshRequest(RAW_REFRESH_TOKEN)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401")
                .hasMessageContaining("이미 사용");

        verify(authRepository, never()).insertRefreshToken(any(RefreshTokenWrite.class));
        verify(cryptoService, never()).secureRandomToken(48);
    }

    @Test
    void loginHistoryRequiresServerSideUserReadPermission() {
        BackofficeOperatorRow operator = new BackofficeOperatorRow(
                100L,
                "biz-admin",
                "업무 관리자",
                "password-hash",
                "MBW_MANAGER",
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
                "backoffice-test-secret-must-be-at-least-32-characters",
                "CPF-MBW",
                "CPF-MBW"))
                .thenReturn(new CmnJwtValidationResult(
                        true,
                        "검증 성공",
                        "100",
                        "CPF-MBW",
                        "CPF-MBW",
                        Instant.now().plusSeconds(600),
                        Map.of("loginId", "biz-admin", "loginDomain", "MBW")));
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator));
        when(authRepository.findLoginHistories(500)).thenReturn(List.of(Map.of("loginResult", "SUCCESS")));

        List<BackofficeLoginHistoryResponse> result = service.loginHistories("Bearer access-token", 1000);

        assertThat(result).hasSize(1);
        verify(authRepository).findLoginHistories(500);
    }

    @Test
    void revokeSessionUsesTokenOwnerAndWritesReasonAudit() {
        BackofficeOperatorRow operator = operator("Y", "N", 0);
        when(jwtService.validateHs256Token(
                "access-token",
                "backoffice-test-secret-must-be-at-least-32-characters",
                "CPF-MBW",
                "CPF-MBW"))
                .thenReturn(new CmnJwtValidationResult(
                        true,
                        "검증 성공",
                        "100",
                        "CPF-MBW",
                        "CPF-MBW",
                        Instant.now().plusSeconds(600),
                        Map.of("loginId", "biz-admin", "loginDomain", "MBW")));
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator));
        when(authRepository.revokeRefreshSession(77L, 100L, "biz-admin")).thenReturn(1);

        BackofficeSessionRevokeResponse result =
                service.revokeSession("Bearer access-token", 77L, "분실 단말 세션 폐기");

        assertThat(result.sessionId()).isEqualTo(77L);
        assertThat(result.revoked()).isTrue();
        verify(auditService).record(
                "biz-admin",
                "SESSION_REVOKE",
                "mbw_refresh_token",
                "77",
                "분실 단말 세션 폐기",
                null,
                Map.of("revokedYn", "Y"));
    }

    private BackofficeOperatorRow operator(String useYn, String lockYn, int failCount) {
        return new BackofficeOperatorRow(
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
