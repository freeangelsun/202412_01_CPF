package cpf.exs.operation.service;

import cpf.cmn.sec.crypto.CmnCryptoService;
import cpf.exs.operation.repository.ExsOperationRepository;
import cpf.exs.operation.repository.ExsOperationRepository.ExchangeLogWrite;
import cpf.exs.operation.repository.ExsOperationRepository.RetryWrite;
import cpf.exs.operation.repository.ExsOperationRepository.TokenEventWrite;
import cpf.exs.operation.repository.ExsOperationRepository.TokenWrite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExsOperationServiceTest {

    private static final String RAW_TOKEN = "abcdefghijklmnopqrstuvwx1234567890ABCDEFGHIJKL";
    private static final String TOKEN_HASH = "hash-value-for-test";

    private final CmnCryptoService cryptoService = mock(CmnCryptoService.class);
    private final ExsOperationRepository repository = mock(ExsOperationRepository.class);
    private final ExsOperationService service = new ExsOperationService(cryptoService, repository);

    @Test
    void refreshTokenRequiresReasonBeforeWritingToken() {
        // 토큰 갱신 사유가 없으면 token hash 저장과 이벤트 저장을 모두 수행하지 않아야 합니다.
        ExsOperationService.TokenRefreshRequest request =
                new ExsOperationService.TokenRefreshRequest("BANK01_API", "access-token", 3600, " ", "exs-operator");

        assertThatThrownBy(() -> service.refreshToken(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason");

        verify(repository, never()).upsertToken(any(TokenWrite.class));
        verify(repository, never()).insertTokenEvent(any(TokenEventWrite.class));
    }

    @Test
    void refreshTokenStoresHashAndMaskedTokenOnly() {
        // 원문 token은 저장소와 응답에 노출하지 않고 hash와 마스킹 값만 전달해야 합니다.
        when(cryptoService.secureRandomToken(48)).thenReturn(RAW_TOKEN);
        when(cryptoService.sha256Base64Url(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        ExsOperationService.TokenRefreshRequest request =
                new ExsOperationService.TokenRefreshRequest("BANK01_API", "access-token", 3600, "정기 갱신", "exs-operator");

        Map<String, Object> response = service.refreshToken(request);

        ArgumentCaptor<TokenWrite> tokenCaptor = ArgumentCaptor.forClass(TokenWrite.class);
        ArgumentCaptor<TokenEventWrite> eventCaptor = ArgumentCaptor.forClass(TokenEventWrite.class);
        verify(repository).upsertToken(tokenCaptor.capture());
        verify(repository).insertTokenEvent(eventCaptor.capture());

        TokenWrite token = tokenCaptor.getValue();
        TokenEventWrite event = eventCaptor.getValue();
        assertThat(token.authProfileCode()).isEqualTo("BANK01_API");
        assertThat(token.tokenHash()).isEqualTo(TOKEN_HASH);
        assertThat(token.maskedToken()).isNotEqualTo(RAW_TOKEN);
        assertThat(token.maskedToken()).contains("****");
        assertThat(event.reason()).isEqualTo("정기 갱신");
        assertThat(event.requestUser()).isEqualTo("exs-operator");
        assertThat(response).doesNotContainValue(RAW_TOKEN);
        assertThat(response.get("tokenHashPreview")).isEqualTo("hash-value-f...");
    }

    @Test
    void requestRetryRequiresReasonBeforeWritingRetryLog() {
        // 재처리 요청도 감사 사유가 없으면 이력 적재를 차단해야 합니다.
        ExsOperationService.RetryRequest request =
                new ExsOperationService.RetryRequest("TGID-001", "EXT-001", "", "exs-operator");

        assertThatThrownBy(() -> service.requestRetry(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason");

        verify(repository, never()).insertRetry(any(RetryWrite.class));
    }

    @Test
    void receiveInboundUsesOfficialUriAndDefaults() {
        // 수신 전문 사전 적재는 공식 URI, 기본 기관/채널, 재처리 불가 기본값으로 저장합니다.
        when(repository.saveExchangeLog(any(ExchangeLogWrite.class))).thenReturn(Map.of("status", "PRE_SAVED"));

        service.receiveInbound("TGID-002", Map.of("externalTransactionId", "EXT-002", "messageSummary", "입금 통지"));

        ArgumentCaptor<ExchangeLogWrite> exchangeCaptor = ArgumentCaptor.forClass(ExchangeLogWrite.class);
        verify(repository).saveExchangeLog(exchangeCaptor.capture());
        ExchangeLogWrite row = exchangeCaptor.getValue();
        assertThat(row.transactionGlobalId()).isEqualTo("TGID-002");
        assertThat(row.externalTransactionId()).isEqualTo("EXT-002");
        assertThat(row.direction()).isEqualTo("INBOUND");
        assertThat(row.httpMethod()).isEqualTo("POST");
        assertThat(row.requestUri()).isEqualTo("/api/exs/inbound");
        assertThat(row.retryableYn()).isEqualTo("N");
        assertThat(row.messageSummary()).isEqualTo("입금 통지");
    }
}
