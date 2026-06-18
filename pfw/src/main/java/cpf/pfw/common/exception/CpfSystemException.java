package cpf.pfw.common.exception;

/**
 * ?쒖뒪???대? ?ㅻ쪟瑜??쒗쁽?섎뒗 ?쒖? ?덉쇅?낅땲??
 */
public class CpfSystemException extends CpfException {
    public CpfSystemException(String detail, Throwable cause) {
        super(CpfErrorCode.INTERNAL_SERVER_ERROR, null, null, detail, cause);
    }
}

