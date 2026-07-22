package com.cpf.common.cde.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 공통 코드 등록/수정 요청 DTO입니다.
 */
@Data
public class CommonCodeRequest {
    /** 수정 대상 코드 ID입니다. 등록 시에는 DB에서 자동 생성됩니다. */
    private Long codeId;

    /** 상위 코드 ID입니다. 코드 그룹이면 null을 사용합니다. */
    private Long parentId;

    /** 코드 그룹 키입니다. 예: USER_STATUS */
    @NotBlank(message = "코드 그룹 키는 필수입니다.")
    private String codeKey;

    /** 코드 값입니다. 예: ACTIVE */
    @NotBlank(message = "코드 값은 필수입니다.")
    private String codeValue;

    /** 코드 설명입니다. */
    private String description;

    /** 사용 여부입니다. Y 또는 N을 사용합니다. */
    private String useYn = "Y";

    /** 요청자 ID입니다. */
    private String requestUser = "SYSTEM";

    /** ADM 변경 감사 사유입니다. */
    private String reason;
}
