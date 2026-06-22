package cpf.bizadm.auth.service;

import cpf.bizadm.auth.repository.BizAdmAuthRepository;
import cpf.bizadm.auth.repository.BizAdmAuthRepository.BizAdmOperatorRow;
import cpf.bizadm.auth.repository.BizAdmAuthRepository.LoginHistoryWrite;
import cpf.bizadm.auth.repository.BizAdmAuthRepository.RefreshTokenWrite;
import cpf.cmn.sec.crypto.CmnCryptoService;
import cpf.cmn.sec.token.CmnJwtCreateRequest;
import cpf.cmn.sec.token.CmnJwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BizAdmAuthServiceTest {

    private static final String RAW_REFRESH_TOKEN = "raw-bizadm-refresh-token-value";
    private static final String REFRESH_TOKEN_HASH = "hash-bizadm-refresh-token-value";

    private final CmnJwtService jwtService = mock(CmnJwtService.class);
    private final CmnCryptoService cryptoService = mock(CmnCryptoService.class);
    private final BizAdmAuthRepository authRepository = mock(BizAdmAuthRepository.class);
    private final BizAdmAuthService service = new BizAdmAuthService(
            jwtService,
            cryptoService,
            authRepository,
            "bizadm-test-secret",
            600,
            7200,
            "BIZ",
            "bizAP01");

    @Test
    void loginStoresRefreshTokenHashAndSuccessHistory() {
        // 로그인 성공 시 refresh token 원문은 응답으로만 전달하고 DB에는 hash만 저장해야 합니다.
        BizAdmOperatorRow operator = operator("Y", "N", 0);
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator));
        when(cryptoService.pbkdf2Matches("password", "password-hash")).thenReturn(true);
        when(jwtService.createHs256Token(any(CmnJwtCreateRequest.class))).thenReturn("access-token");
        when(cryptoService.secureRandomToken(48)).thenReturn(RAW_REFRESH_TOKEN);
        when(cryptoService.sha256Base64Url(RAW_REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_HASH);

        BizAdmAuthService.LoginResult result = service.login(
                new BizAdmAuthService.LoginRequest("biz-admin", "password"),
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
        BizAdmOperatorRow operator = operator("Y", "N", 1);
        when(authRepository.findOperatorByLoginId("biz-admin")).thenReturn(Optional.of(operator));
        when(cryptoService.pbkdf2Matches("wrong", "password-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(
                new BizAdmAuthService.LoginRequest("biz-admin", "wrong"),
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

    private BizAdmOperatorRow operator(String useYn, String lockYn, int failCount) {
        return new BizAdmOperatorRow(
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
