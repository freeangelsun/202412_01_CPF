package cpf.pfw.common.exception;

/**
 * ? 二쇱젣?곸뿭 ?먮뒗 ?몃? ?쒖뒪???몄텧 ?ㅽ뙣瑜??쒗쁽?섎뒗 ?쒖? ?덉쇅?낅땲??
 */
public class CpfExternalServiceException extends CpfException {
    public CpfExternalServiceException(String detail, Throwable cause) {
        super(CpfErrorCode.EXTERNAL_SERVICE_ERROR, null, null, detail, cause);
    }
}

