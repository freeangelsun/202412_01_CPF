package cpf.cmn.msg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Common message create/update request.
 */
@Data
public class CommonMessageRequest {
    private Long messageId;

    /** Standard message code, for example MPFW900001 or MMBR010001. */
    private String messageCode;

    /** Backward compatible alias for older callers. */
    private String messageKey;

    @NotBlank(message = "locale is required.")
    private String locale;

    /** FIXED or INDEXED. INDEXED uses {0}, {1}, ... placeholders. */
    private String messageFormatType = "FIXED";

    /** Customer/display message. */
    private String externalMessage;

    /** Operator/log diagnostic message. */
    private String internalMessage;

    /** Backward compatible alias for older callers. */
    private String messageValue;

    private Integer parameterCount = 0;

    /** JSON or text sample, for example ["fieldName","uri"]. */
    private String parameterSample;

    private String description;
    private String useYn = "Y";
    private String requestUser = "SYSTEM";

    public String getEffectiveMessageCode() {
        return hasText(messageCode) ? messageCode : messageKey;
    }

    public String getEffectiveExternalMessage() {
        return hasText(externalMessage) ? externalMessage : messageValue;
    }

    public String getEffectiveInternalMessage() {
        return hasText(internalMessage) ? internalMessage : getEffectiveExternalMessage();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

