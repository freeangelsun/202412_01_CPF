package cpf.xyz.edu.crud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * XYZ CRUD 교육 항목 등록/수정 요청 DTO입니다.
 *
 * @param title 항목명
 * @param description 항목 설명
 * @param requestUser 요청 사용자
 * @param categoryCode 교육 분류 코드
 * @param ownerMemberNo 예시 소유 회원 번호
 */
public record XyzCrudEducationRequest(
        @NotBlank(message = "항목명은 필수입니다.")
        @Size(max = 100, message = "항목명은 100자 이하로 입력해야 합니다.")
        String title,

        @Size(max = 200, message = "설명은 200자 이하로 입력해야 합니다.")
        String description,

        @Size(max = 100, message = "요청 사용자는 100자 이하로 입력해야 합니다.")
        String requestUser,

        @Pattern(regexp = "^[A-Z0-9_]{1,30}$", message = "분류 코드는 영문 대문자, 숫자, 밑줄만 사용할 수 있습니다.")
        String categoryCode,

        @Size(max = 50, message = "소유 회원 번호는 50자 이하로 입력해야 합니다.")
        String ownerMemberNo) {

    public XyzCrudEducationRequest(String title, String description, String requestUser) {
        this(title, description, requestUser, null, null);
    }
}
