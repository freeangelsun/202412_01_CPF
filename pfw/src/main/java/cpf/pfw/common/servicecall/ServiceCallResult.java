package cpf.pfw.common.servicecall;

/**
 * 서비스 호출 엔진 실행 결과입니다.
 *
 * <p>응답 본문, 선택 instance, HTTP status, 실패 사유를 분리해 ADM 관제와 call history가
 * 같은 기준으로 조회할 수 있게 합니다.</p>
 */
public record ServiceCallResult<T>(
        String status,
        ServiceCallResolvedTarget target,
        T responseBody,
        Integer httpStatus,
        Long durationMillis,
        Integer attemptCount,
        String failureCode,
        String failureMessage) {

    public static <T> ServiceCallResult<T> success(
            ServiceCallResolvedTarget target,
            T responseBody,
            Integer httpStatus,
            Long durationMillis,
            Integer attemptCount) {
        return new ServiceCallResult<>("SUCCESS", target, responseBody, httpStatus, durationMillis, attemptCount, null, null);
    }

    public static <T> ServiceCallResult<T> success(
            ServiceCallResolvedTarget target,
            T responseBody,
            Integer httpStatus,
            Long durationMillis) {
        return success(target, responseBody, httpStatus, durationMillis, 1);
    }

    public static <T> ServiceCallResult<T> failure(
            ServiceCallResolvedTarget target,
            Integer httpStatus,
            Long durationMillis,
            Integer attemptCount,
            String failureCode,
            String failureMessage) {
        return new ServiceCallResult<>("FAILED", target, null, httpStatus, durationMillis, attemptCount, failureCode, failureMessage);
    }

    public static <T> ServiceCallResult<T> failure(
            ServiceCallResolvedTarget target,
            Long durationMillis,
            String failureCode,
            String failureMessage) {
        return failure(target, null, durationMillis, 1, failureCode, failureMessage);
    }
}
