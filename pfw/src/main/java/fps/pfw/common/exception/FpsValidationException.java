package fps.pfw.common.exception;

import java.util.Map;

/**
 * 개발자가 명시적으로 입력값 검증 실패를 표현할 때 사용하는 표준 예외입니다.
 */
public class FpsValidationException extends FpsException {
    public FpsValidationException(String detail) {
        super(FpsErrorCode.INVALID_PARAMETER, detail);
    }

    public FpsValidationException(FpsErrorDefinition errorCode, String detail, Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }
}
