package cpf.pfw.common.exception;

import java.util.Map;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 */
public class CpfBusinessException extends CpfException {
    public CpfBusinessException(String detail) {
        super(CpfErrorCode.BUSINESS_RULE_VIOLATION, detail);
    }

    public CpfBusinessException(CpfErrorDefinition errorCode, String detail) {
        super(errorCode, detail);
    }

    public CpfBusinessException(CpfErrorDefinition errorCode, String detail, Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }

    public CpfBusinessException(String responseCode, String detail, Map<String, Object> messageArguments) {
        super(responseCode, detail, messageArguments);
    }

    public CpfBusinessException(
            CpfErrorDefinition errorCode,
            String externalMessage,
            String internalMessage,
            String detail,
            Map<String, Object> messageArguments) {
        super(errorCode, externalMessage, internalMessage, detail, null, messageArguments);
    }
}

