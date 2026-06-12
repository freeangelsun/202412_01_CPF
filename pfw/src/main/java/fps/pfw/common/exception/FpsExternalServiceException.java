package fps.pfw.common.exception;

/**
 * 타 주제영역 또는 외부 시스템 호출 실패를 표현하는 표준 예외입니다.
 */
public class FpsExternalServiceException extends FpsException {
    public FpsExternalServiceException(String detail, Throwable cause) {
        super(FpsErrorCode.EXTERNAL_SERVICE_ERROR, null, null, detail, cause);
    }
}
