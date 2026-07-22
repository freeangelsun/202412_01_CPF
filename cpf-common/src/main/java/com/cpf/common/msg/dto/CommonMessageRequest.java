package cpf.cmn.msg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 공통 메시지 등록/수정 요청 DTO입니다.
 */
@Data
public class CommonMessageRequest {
    private Long messageId;

    /** 표준 메시지 코드입니다. 예: MPFW900001 또는 MMBR010001 */
    private String messageCode;

    /** 기존 호출자 호환을 위한 메시지 키 별칭입니다. */
    private String messageKey;

    @NotBlank(message = "locale은 필수입니다.")
    private String locale;

    /** 메시지 포맷 유형입니다. INDEXED는 {0}, {1} placeholder를 사용합니다. */
    private String messageFormatType = "FIXED";

    /** 외부 응답 또는 화면에 노출할 메시지입니다. */
    private String externalMessage;

    /** 내부 로그와 운영자 진단용 메시지입니다. */
    private String internalMessage;

    /** 기존 호출자 호환을 위한 메시지 값 별칭입니다. */
    private String messageValue;

    private Integer parameterCount = 0;

    /** 파라미터 예시입니다. 예: ["fieldName","uri"] */
    private String parameterSample;

    private String description;
    private String useYn = "Y";
    private String requestUser = "SYSTEM";
    private String reason;

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
