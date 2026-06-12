package fps.acc.bse.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 클래스: AccMemberDTO
 *
 * ACC 모듈의 멤버 데이터를 정의합니다.
 */
@Data
public class AccMemberDTO {
    @NotEmpty(message = "Member ID는 필수 값입니다.")
    private String memberId; // 멤버 ID

    @NotEmpty(message = "Member Name은 필수 값입니다.")
    private String memberName; // 멤버 이름

    @NotEmpty(message = "Member Status는 필수 값입니다.")
    private String memberStatus; // 멤버 상태
}
