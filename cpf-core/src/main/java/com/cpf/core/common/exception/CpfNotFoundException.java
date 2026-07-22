package cpf.pfw.common.exception;

/**
 * CPF 기능 설명입니다.
 */
public class CpfNotFoundException extends CpfException {
    public CpfNotFoundException(String detail) {
        super(CpfErrorCode.NOT_FOUND, detail);
    }
}

