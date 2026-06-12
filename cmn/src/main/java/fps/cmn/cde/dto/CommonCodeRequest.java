package fps.cmn.cde.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 공통 코드 등록/수정 요청 DTO입니다.
 *
 * <p>CMN 코드 관리는 업무 화면의 선택값, 상태값, 구분값처럼 여러 주제영역에서 반복 사용하는 값을
 * DB와 캐시에 저장하기 위한 공통 기능입니다.</p>
 */
@Data
public class CommonCodeRequest {
    /** 등록 후 DB에서 생성된 코드 ID를 담거나, 수정 화면에서 식별값으로 사용할 수 있습니다. */
    private Long codeId;

    /** 상위 코드 ID입니다. 최상위 코드이면 null로 둡니다. */
    private Long parentId;

    /** 코드 그룹 또는 코드 키입니다. 예: USER_STATUS */
    @NotBlank(message = "코드 키는 필수입니다.")
    private String codeKey;

    /** 실제 코드 값입니다. 예: ACTIVE */
    @NotBlank(message = "코드 값은 필수입니다.")
    private String codeValue;

    /** 코드 설명입니다. 화면 표시나 운영자가 의미를 파악할 때 사용합니다. */
    private String description;

    /** 사용 여부입니다. Y이면 사용, N이면 미사용입니다. */
    private String useYn = "Y";

    /** 등록자/수정자로 남길 사용자 ID입니다. 값이 없으면 SYSTEM으로 처리합니다. */
    private String requestUser = "SYSTEM";
}
