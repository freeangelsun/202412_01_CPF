package cpf.pfw.common.exception;

/**
 * CPF 기능 설명입니다.
 */
public class CpfExternalServiceException extends CpfException {
    public CpfExternalServiceException(String detail, Throwable cause) {
        super(CpfErrorCode.EXTERNAL_SERVICE_ERROR, null, null, detail, cause);
    }
}

