package cpf.pfw.common.exception;

import java.util.Map;

/**
 * Exception for request and business input validation failures.
 */
public class FpsValidationException extends FpsException {
    public FpsValidationException(String detail) {
        super(FpsErrorCode.INVALID_PARAMETER, detail);
    }

    public FpsValidationException(FpsErrorDefinition errorCode, String detail, Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }
}
