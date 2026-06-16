package cpf.pfw.common.exception;

import java.util.Map;

/**
 * ?낅Т 洹쒖튃 ?꾨컲???쒗쁽?섎뒗 ?쒖? ?덉쇅?낅땲??
 *
 * <p>?? ?쒕룄 珥덇낵, ?대? 泥섎━???좎껌, ?꾩옱 ?곹깭?먯꽌 ?덉슜?섏? ?딅뒗 泥섎━ ??/p>
 */
public class FpsBusinessException extends FpsException {
    public FpsBusinessException(String detail) {
        super(FpsErrorCode.BUSINESS_RULE_VIOLATION, detail);
    }

    public FpsBusinessException(FpsErrorDefinition errorCode, String detail) {
        super(errorCode, detail);
    }

    public FpsBusinessException(FpsErrorDefinition errorCode, String detail, Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }

    public FpsBusinessException(String responseCode, String detail, Map<String, Object> messageArguments) {
        super(responseCode, detail, messageArguments);
    }

    public FpsBusinessException(
            FpsErrorDefinition errorCode,
            String externalMessage,
            String internalMessage,
            String detail,
            Map<String, Object> messageArguments) {
        super(errorCode, externalMessage, internalMessage, detail, null, messageArguments);
    }
}

