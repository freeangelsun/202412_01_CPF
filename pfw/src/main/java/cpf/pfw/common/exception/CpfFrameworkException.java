package cpf.pfw.common.exception;

import java.util.Map;

/**
 * PFW 프레임워크 계층에서 발생한 오류를 표준 응답코드로 전달하는 예외입니다.
 */
public class CpfFrameworkException extends CpfException {

    public CpfFrameworkException(CpfFrameworkErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public CpfFrameworkException(
            CpfFrameworkErrorCode errorCode,
            String detail,
            Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }
}
