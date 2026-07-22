package cpf.mbr.bse.service;

import cpf.cmn.sec.crypto.CmnCryptoService;
import cpf.cmn.sec.token.CmnJwtCreateRequest;
import cpf.cmn.sec.token.CmnJwtService;
import cpf.mbr.bse.entity.Member;
import cpf.mbr.bse.mapper.MemberMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
@SuppressWarnings("unchecked")
class MbrAuthServiceTest {

    private static final String RAW_REFRESH_TOKEN = "raw-mbr-refresh-token-value";
    private static final String REFRESH_TOKEN_HASH = "hash-mbr-refresh-token-value";

    private final CmnJwtService jwtService = mock(CmnJwtService.class);
    private final CmnCryptoService cryptoService = mock(CmnCryptoService.class);
    private final MemberMapper memberMapper = mock(MemberMapper.class);
    private final MbrAuthService service = new MbrAuthService(
            jwtService,
            cryptoService,
            memberMapper,
            "mbr-test-secret",
            600,
            7200,
            "MBR",
            "local01");

    @Test
    void loginStoresRefreshTokenHashAndLoginHistory() {
        // 회원 로그인 성공 시 refresh token 원문은 DB에 저장하지 않고 hash만 저장합니다.
        Member member = activeMember();
        when(memberMapper.selectMemberByLoginId("mbr001")).thenReturn(Optional.of(member));
        when(cryptoService.pbkdf2Matches("password", "password-hash")).thenReturn(true);
        when(jwtService.createHs256Token(any(CmnJwtCreateRequest.class))).thenReturn("access-token");
        when(cryptoService.secureRandomToken(48)).thenReturn(RAW_REFRESH_TOKEN);
        when(cryptoService.sha256Base64Url(RAW_REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_HASH);

        MbrAuthService.LoginResult result = service.login(
                new MbrAuthService.LoginRequest("mbr001", "password"),
                "127.0.0.1",
                "unit-test");

        ArgumentCaptor<Map<String, Object>> refreshCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, Object>> historyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(memberMapper).markLoginSuccess(1);
        verify(memberMapper).insertRefreshToken(refreshCaptor.capture());
        verify(memberMapper).insertMemberLoginHistory(historyCaptor.capture());

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo(RAW_REFRESH_TOKEN);
        assertThat(refreshCaptor.getValue().get("refreshTokenHash")).isEqualTo(REFRESH_TOKEN_HASH);
        assertThat(refreshCaptor.getValue().get("refreshTokenHash")).isNotEqualTo(RAW_REFRESH_TOKEN);
        assertThat(historyCaptor.getValue().get("loginResult")).isEqualTo("SUCCESS");
        assertThat(historyCaptor.getValue().get("memberNo")).isEqualTo("M000000001");
        assertThat(historyCaptor.getValue().get("customerNo")).isEqualTo("C000000001");
    }

    @Test
    void loginFailureIncreasesFailCountAndDoesNotStoreRefreshToken() {
        // 회원 비밀번호 실패 시 실패 횟수와 실패 이력만 저장하고 refresh token은 만들지 않습니다.
        Member member = activeMember();
        when(memberMapper.selectMemberByLoginId("mbr001")).thenReturn(Optional.of(member));
        when(cryptoService.pbkdf2Matches("wrong", "password-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(
                new MbrAuthService.LoginRequest("mbr001", "wrong"),
                "127.0.0.1",
                "unit-test"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");

        ArgumentCaptor<Map<String, Object>> historyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(memberMapper).increaseLoginFailCount(1);
        verify(memberMapper).insertMemberLoginHistory(historyCaptor.capture());
        verify(memberMapper, never()).insertRefreshToken(any());
        assertThat(historyCaptor.getValue().get("loginResult")).isEqualTo("FAIL");
        assertThat(historyCaptor.getValue().get("failureReason")).isEqualTo("비밀번호 불일치");
    }

    @Test
    void refreshRejectsRevokedToken() {
        // 폐기된 refresh token은 access token 재발급에 사용할 수 없습니다.
        when(cryptoService.sha256Base64Url(RAW_REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_HASH);
        when(memberMapper.selectRefreshTokenByHash(REFRESH_TOKEN_HASH)).thenReturn(Map.of(
                "loginId", "mbr001",
                "revokedYn", "Y",
                "expireAt", LocalDateTime.now().plusHours(1)));

        assertThatThrownBy(() -> service.refresh(new MbrAuthService.RefreshRequest(RAW_REFRESH_TOKEN)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");

        verify(jwtService, never()).createHs256Token(any(CmnJwtCreateRequest.class));
    }

    private Member activeMember() {
        return Member.builder()
                .id(1)
                .memberNo("M000000001")
                .customerNo("C000000001")
                .loginId("mbr001")
                .passwordHash("password-hash")
                .loginFailCount(0)
                .passwordChangeRequiredYn("N")
                .passwordExpireAt(LocalDateTime.now().plusDays(30))
                .name("회원 1")
                .memberStatus("ACTIVE")
                .lockYn("N")
                .withdrawYn("N")
                .channelCode("WEB")
                .build();
    }
}
