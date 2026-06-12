package fps.pfw.common.exception;

import java.util.Map;

/**
 * PFW 프레임워크 코어에서 발생하는 표준 예외입니다.
 *
 * <p>업무 규칙 위반이 아니라 거래 헤더, 거래 메타데이터, 프레임워크 설정처럼
 * 프레임워크 자체 기준을 위반했을 때 사용합니다.</p>
 */
public class FpsFrameworkException extends FpsException {

    public FpsFrameworkException(FpsFrameworkErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public FpsFrameworkException(
            FpsFrameworkErrorCode errorCode,
            String detail,
            Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }
}
