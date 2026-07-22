package cpf.pfw.common.header;

import java.util.Map;

/**
 * ADM 로그 상세에서 헤더부를 수신/해석/전파 관점으로 나누어 보여주기 위한 스냅샷입니다.
 */
public record CpfHeaderSnapshot(
        Map<String, String> inboundHeaders,
        Map<String, String> resolvedHeaders,
        Map<String, String> outboundHeaders,
        Map<String, String> responseHeaders) {
}
