package cpf.pfw.common.exception;

/**
 * ?쒖뒪???대? ?ㅻ쪟瑜??쒗쁽?섎뒗 ?쒖? ?덉쇅?낅땲??
 */
public class FpsSystemException extends FpsException {
    public FpsSystemException(String detail, Throwable cause) {
        super(FpsErrorCode.INTERNAL_SERVER_ERROR, null, null, detail, cause);
    }
}

