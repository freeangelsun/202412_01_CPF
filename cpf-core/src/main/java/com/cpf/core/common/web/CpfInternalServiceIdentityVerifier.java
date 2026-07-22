package cpf.pfw.common.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * CPF 내부 공유 API 호출자의 서비스 신원을 검증하는 확장 경계입니다.
 *
 * <p>운영 환경에서는 mTLS 인증서, 서비스 메시 신원 또는 검증된 서비스 토큰을 확인하는
 * 어댑터를 빈으로 등록해야 합니다. PFW는 검증 구현이 없을 때 운영 프로필을 기본 거부하고,
 * local/dev/test 프로필의 loopback 호출만 개발 편의 목적으로 허용합니다.</p>
 */
@FunctionalInterface
public interface CpfInternalServiceIdentityVerifier {

    /**
     * 수신 요청의 호출 서비스와 인스턴스 신원이 신뢰 가능한지 반환합니다.
     *
     * @param request 수신 HTTP 요청
     * @param callerServiceId 호출 서비스 ID
     * @param callerInstanceId 호출 서비스 인스턴스 ID
     * @return 신원이 검증되면 {@code true}
     */
    boolean isTrusted(
            HttpServletRequest request,
            String callerServiceId,
            String callerInstanceId);
}
