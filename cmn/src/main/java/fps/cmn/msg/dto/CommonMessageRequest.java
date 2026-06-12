package fps.cmn.msg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 공통 메시지 등록/수정 요청 DTO입니다.
 *
 * <p>메시지는 오류 문구, 안내 문구, 다국어 문구처럼 여러 주제영역이 같은 키로 재사용하는 텍스트입니다.
 * 메시지 값이 변경되면 캐시를 즉시 리프레시해 실행 중인 업무 API가 최신 문구를 사용하게 합니다.</p>
 */
@Data
public class CommonMessageRequest {
    /** 등록 후 DB에서 생성된 메시지 ID를 담거나, 수정 화면에서 식별값으로 사용할 수 있습니다. */
    private Long messageId;

    /** 메시지 키입니다. 예: WELCOME_MSG */
    @NotBlank(message = "메시지 키는 필수입니다.")
    private String messageKey;

    /** 메시지 내용입니다. */
    @NotBlank(message = "메시지 값은 필수입니다.")
    private String messageValue;

    /** 언어 코드입니다. 예: ko, en */
    @NotBlank(message = "언어 코드는 필수입니다.")
    private String locale;

    /**
     * 메시지 용도입니다.
     *
     * <p>EXTERNAL은 고객/화면에 내려보내는 메시지,
     * INTERNAL은 로그와 운영자 화면에서 확인하는 내부 메시지입니다.</p>
     */
    private String messageType = "EXTERNAL";

    /** 메시지 설명입니다. 운영자가 문구 목적을 이해할 때 사용합니다. */
    private String description;

    /** 사용 여부입니다. Y이면 사용, N이면 미사용입니다. */
    private String useYn = "Y";

    /** 등록자/수정자로 남길 사용자 ID입니다. 값이 없으면 SYSTEM으로 처리합니다. */
    private String requestUser = "SYSTEM";
}
