package cpf.pfw.common.exception;

import java.util.Map;

/**
 * 媛쒕컻?먭? 紐낆떆?곸쑝濡??낅젰媛?寃利??ㅽ뙣瑜??쒗쁽?????ъ슜?섎뒗 ?쒖? ?덉쇅?낅땲??
 */
public class FpsValidationException extends FpsException {
    public FpsValidationException(String detail) {
        super(FpsErrorCode.INVALID_PARAMETER, detail);
    }

    public FpsValidationException(FpsErrorDefinition errorCode, String detail, Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }
}

