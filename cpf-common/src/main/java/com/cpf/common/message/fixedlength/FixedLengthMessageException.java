package cpf.cmn.message.fixedlength;

import java.util.List;

/**
 * 고정길이 전문 parse/format 과정에서 즉시 중단해야 하는 오류를 표현합니다.
 */
public class FixedLengthMessageException extends RuntimeException {
    private final List<FixedLengthMessageError> errors;

    public FixedLengthMessageException(String message, List<FixedLengthMessageError> errors) {
        super(message);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public List<FixedLengthMessageError> getErrors() {
        return errors;
    }
}
