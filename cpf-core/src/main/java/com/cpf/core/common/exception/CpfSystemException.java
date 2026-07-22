package cpf.pfw.common.exception;

/**
 * CPF 기능 설명입니다.
 */
public class CpfSystemException extends CpfException {
    public CpfSystemException(String detail, Throwable cause) {
        super(CpfErrorCode.INTERNAL_SERVER_ERROR, null, null, detail, cause);
    }
}

