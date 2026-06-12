package fps.cmn.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 공통 설정값 등록/수정 요청 DTO입니다.
 *
 * <p>설정값은 업무 토글, 한도, 연계 설정 키처럼 배포 없이 운영 중 변경될 수 있는 값을 관리합니다.
 * 민감 설정은 별도 암호화 정책을 붙일 수 있도록 암호화 여부 컬럼을 함께 둡니다.</p>
 */
@Data
public class CommonConfigRequest {
    /** 등록 후 DB에서 생성된 설정 ID를 담거나, 수정 화면에서 식별값으로 사용할 수 있습니다. */
    private Long configId;

    /** 설정 키입니다. 예: FPS.LOGIN.MAX_FAIL_COUNT */
    @NotBlank(message = "설정 키는 필수입니다.")
    private String configKey;

    /** 설정 값입니다. */
    @NotBlank(message = "설정 값은 필수입니다.")
    private String configValue;

    /** 설정 타입입니다. 예: STRING, NUMBER, BOOLEAN */
    private String configType = "STRING";

    /** 설정 설명입니다. */
    private String description;

    /** 암호화 저장 여부입니다. 현재는 컬럼만 준비하며 실제 암호화는 후속 보안 기능에서 구현합니다. */
    private String encryptedYn = "N";

    /** 사용 여부입니다. Y이면 사용, N이면 미사용입니다. */
    private String useYn = "Y";

    /** 등록자/수정자로 남길 사용자 ID입니다. 값이 없으면 SYSTEM으로 처리합니다. */
    private String requestUser = "SYSTEM";
}
