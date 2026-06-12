package fps.pfw.common.exception;

/**
 * 시스템 내부 오류를 표현하는 표준 예외입니다.
 */
public class FpsSystemException extends FpsException {
    public FpsSystemException(String detail, Throwable cause) {
        super(FpsErrorCode.INTERNAL_SERVER_ERROR, null, null, detail, cause);
    }
}
