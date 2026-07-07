package cpf.pfw.common.servicecall;

/**
 * 서비스 호출 엔진 실행 결과입니다.
 *
 * <p>응답 본문을 노출하기보다 호출 상태, 대상, HTTP status, 실패 사유를 분리해
 * ADM 조회와 감사/로그 정책이 같은 구조로 사용할 수 있게 합니다.</p>
 */
public record ServiceCallResult<T>(
        String status,
        ServiceCallResolvedTarget target,
        T responseBody,
        Integer httpStatus,
        Long durationMillis,
        String failureCode,
        String failureMessage) {

    public static <T> ServiceCallResult<T> success(
            ServiceCallResolvedTarget target,
            T responseBody,
            Integer httpStatus,
            Long durationMillis) {
        return new ServiceCallResult<>("SUCCESS", target, responseBody, httpStatus, durationMillis, null, null);
    }

    public static <T> ServiceCallResult<T> failure(
            ServiceCallResolvedTarget target,
            Long durationMillis,
            String failureCode,
            String failureMessage) {
        return new ServiceCallResult<>("FAILED", target, null, null, durationMillis, failureCode, failureMessage);
    }
}
