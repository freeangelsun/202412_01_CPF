package cpf.cmn.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 공통 설정 등록/수정 요청 DTO입니다.
 */
@Data
public class CommonConfigRequest {
    /** 수정 대상 설정 ID입니다. 등록 시에는 DB에서 자동 생성됩니다. */
    private Long configId;

    /** 설정 키입니다. 예: CPF.ADM.PASSWORD_MAX_FAIL_COUNT */
    @NotBlank(message = "설정 키는 필수입니다.")
    private String configKey;

    /** 설정 값입니다. */
    @NotBlank(message = "설정 값은 필수입니다.")
    private String configValue;

    /** 설정 값 유형입니다. 예: STRING, NUMBER, BOOLEAN */
    private String configType = "STRING";

    /** 설정 설명입니다. */
    private String description;

    /** 암호화 값 여부입니다. ADM 조회 시 값은 마스킹합니다. */
    private String encryptedYn = "N";

    /** 사용 여부입니다. Y 또는 N을 사용합니다. */
    private String useYn = "Y";

    /** 요청자 ID입니다. */
    private String requestUser = "SYSTEM";

    /** ADM 변경 감사 사유입니다. */
    private String reason;
}
