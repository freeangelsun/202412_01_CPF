package cpf.pfw.common.exception;

import java.util.Map;

/**
 * ?낅Т 洹쒖튃 ?꾨컲???쒗쁽?섎뒗 ?쒖? ?덉쇅?낅땲??
 *
 * <p>?? ?쒕룄 珥덇낵, ?대? 泥섎━???좎껌, ?꾩옱 ?곹깭?먯꽌 ?덉슜?섏? ?딅뒗 泥섎━ ??/p>
 */
public class CpfBusinessException extends CpfException {
    public CpfBusinessException(String detail) {
        super(CpfErrorCode.BUSINESS_RULE_VIOLATION, detail);
    }

    public CpfBusinessException(CpfErrorDefinition errorCode, String detail) {
        super(errorCode, detail);
    }

    public CpfBusinessException(CpfErrorDefinition errorCode, String detail, Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }

    public CpfBusinessException(String responseCode, String detail, Map<String, Object> messageArguments) {
        super(responseCode, detail, messageArguments);
    }

    public CpfBusinessException(
            CpfErrorDefinition errorCode,
            String externalMessage,
            String internalMessage,
            String detail,
            Map<String, Object> messageArguments) {
        super(errorCode, externalMessage, internalMessage, detail, null, messageArguments);
    }
}

