package com.cpf.bizadmin.auth.service;

import com.cpf.bizadmin.auth.repository.BzaAuthRepository;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository.BzaOperatorRow;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository.LoginHistoryWrite;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository.RefreshTokenRow;
import com.cpf.bizadmin.auth.repository.BzaAuthRepository.RefreshTokenWrite;
import com.cpf.common.sec.crypto.CmnCryptoService;
import com.cpf.common.sec.token.CmnJwtCreateRequest;
import com.cpf.common.sec.token.CmnJwtService;
import com.cpf.common.sec.token.CmnJwtValidationResult;
import com.cpf.core.common.security.password.CpfPasswordHashingPort;
import com.cpf.core.common.security.password.CpfPasswordVerification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private final CpfPasswordHashingPort passwordHashingPort = mock(CpfPasswordHashingPort.class);
    private final BzaAuthRepository authRepository = mock(BzaAuthRepository.class);
    private final BzaAuthService service = new BzaAuthService(
            jwtService,
            cryptoService,
            passwordHashingPort,
            authRepository,
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

        BzaAuthService.LoginResult result = service.login(
                new BzaAuthService.LoginRequest("biz-admin", "password"),
                "127.0.0.1",
                "unit-test");

        ArgumentCaptor<RefreshTokenWrite> refreshCaptor = ArgumentCaptor.forClass(RefreshTokenWrite.class);
        ArgumentCaptor<LoginHistoryWrite> historyCaptor = ArgumentCaptor.forClass(LoginHistoryWrite.class);
        verify(authRepository).markLoginSuccess(100L);
        verify(authRepository).insertRefreshToken(refreshCaptor.capture());
        verify(authRepository).insertLoginHistory(historyCaptor.capture());

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo(RAW_REFRESH_TOKEN);
        assertThat(refreshCaptor.getValue().refreshTokenHash()).isEqualTo(REFRESH_TOKEN_HASH);
        assertThat(refreshCaptor.getValue().refreshTokenHash()).isNotEqualTo(RAW_REFRESH_TOKEN);
        assertThat(historyCaptor.getValue().loginResult()).isEqualTo("SUCCESS");
        assertThat(historyCaptor.getValue().failureReason()).isNull();
    }

    @Test
    void loginFailureIncreasesFailCountAndDoesNotStoreToken() {
        // 비밀번호 실패 시 실패 횟수와 로그인 실패 이력만 남기고 refresh token은 만들지 않습니다.
        BzaOperatorRow operator = operator("Y", "N", 1);
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator));
        when(passwordHashingPort.verify(any(char[].class), org.mockito.ArgumentMatchers.eq("password-hash")))
                .thenReturn(CpfPasswordVerification.rejected());

        assertThatThrownBy(() -> service.login(
                new BzaAuthService.LoginRequest("biz-admin", "wrong"),
                "127.0.0.1",
                "unit-test"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");

        ArgumentCaptor<LoginHistoryWrite> historyCaptor = ArgumentCaptor.forClass(LoginHistoryWrite.class);
        verify(authRepository).increaseLoginFailCount(100L);
        verify(authRepository).insertLoginHistory(historyCaptor.capture());
        verify(authRepository, never()).insertRefreshToken(any(RefreshTokenWrite.class));
        assertThat(historyCaptor.getValue().loginResult()).isEqualTo("FAIL");
        assertThat(historyCaptor.getValue().failureReason()).isEqualTo("비밀번호 불일치");
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
                "Y",
                "N",
                0,
                "N",
                Instant.now().plusSeconds(86400),
                null,
                List.of("USER"),
                List.of("USER:READ"));
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

        List<Map<String, Object>> result = service.loginHistories("Bearer access-token", 1000);

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

        Map<String, Object> result = service.revokeSession("Bearer access-token", 77L, "분실 단말 세션 폐기");

        assertThat(result).containsEntry("sessionId", 77L).containsEntry("revokedYn", "Y");
        verify(authRepository).insertBusinessAudit(org.mockito.ArgumentMatchers.argThat(values ->
                "biz-admin".equals(values.get("actorId"))
                        && "분실 단말 세션 폐기".equals(values.get("reason"))
                        && "SESSION_REVOKE".equals(values.get("actionType"))));
    }

    private BzaOperatorRow operator(String useYn, String lockYn, int failCount) {
        return new BzaOperatorRow(
                100L,
                "biz-admin",
                "업무 관리자",
                "password-hash",
                "BIZ_MANAGER",
                useYn,
                lockYn,
                failCount,
                "N",
                Instant.now().plusSeconds(86400),
                null,
                List.of("CUSTOMER"),
                List.of("CUSTOMER:READ"));
    }
}
