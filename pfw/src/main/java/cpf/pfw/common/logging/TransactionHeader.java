package cpf.pfw.common.logging;

import lombok.Builder;
import lombok.Value;

/**
 * 한 요청에서 수집한 CPF 표준 거래 헤더입니다.
 *
 * <p>필수 헤더는 {@code X-Request-Type}, {@code X-Original-Channel-Code},
 * {@code X-Channel-Code}이고, 나머지 값은 클라이언트/호출자/멱등성 추적을
 * 위해 가능한 경우 함께 주고받는 표준 선택 헤더입니다.</p>
 */
@Value
@Builder
public class TransactionHeader {
    String apiVersion;
    String clientAppId;
    String clientVersion;
    String callerService;
    String callerInstanceId;
    String correlationId;
    String idempotencyKey;
    String locale;
    String timezone;
    String requestType;
    String originalChannelCode;
    String channelCode;
    String memberNo;
    String customerNo;
    String userId;
    String screenId;
    String deviceId;
    String clientRequestTime;
    String clientIp;
    String reservedField1;
    String reservedField2;
    String reservedField3;
    String reservedField4;
    String reservedField5;
    String wasId;
}

