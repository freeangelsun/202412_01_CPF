package fps.pfw.common.exception;

import java.util.Map;

/**
 * 업무 규칙 위반을 표현하는 표준 예외입니다.
 *
 * <p>예: 한도 초과, 이미 처리된 신청, 현재 상태에서 허용되지 않는 처리 등</p>
 */
public class FpsBusinessException extends FpsException {
    public FpsBusinessException(String detail) {
        super(FpsErrorCode.BUSINESS_RULE_VIOLATION, detail);
    }

    public FpsBusinessException(FpsErrorDefinition errorCode, String detail) {
        super(errorCode, detail);
    }

    public FpsBusinessException(FpsErrorDefinition errorCode, String detail, Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }

    public FpsBusinessException(
            FpsErrorDefinition errorCode,
            String externalMessage,
            String internalMessage,
            String detail,
            Map<String, Object> messageArguments) {
        super(errorCode, externalMessage, internalMessage, detail, null, messageArguments);
    }
}
