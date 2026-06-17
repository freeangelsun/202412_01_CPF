package cpf.mbr.bse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MBR 회원 API 요청/응답 DTO입니다.
 *
 * <p>EDU 샘플에서는 필수 입력 검증, 공통 응답, 거래 로그, ADM 회원관리 연동을
 * 함께 확인할 수 있도록 회원 기본 운영 필드를 포함합니다.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MbrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 회원 내부 순번입니다. */
    private Integer memberId;

    /** 회원번호입니다. 비워서 생성하면 서비스에서 자동 발급합니다. */
    @Size(max = 30, message = "회원번호는 30자 이하여야 합니다.")
    private String memberNo;

    /** 고객번호입니다. */
    @Size(max = 30, message = "고객번호는 30자 이하여야 합니다.")
    private String customerNo;

    /** 로그인 ID입니다. */
    @Size(max = 100, message = "로그인 ID는 100자 이하여야 합니다.")
    private String loginId;

    /** 회원명입니다. */
    @NotBlank(message = "회원명은 필수입니다.", groups = CreateGroup.class)
    @Size(min = 1, max = 100, message = "회원명은 1자 이상 100자 이하여야 합니다.", groups = CreateGroup.class)
    private String memberName;

    /** 이메일 주소입니다. */
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 200, message = "이메일은 200자 이하여야 합니다.")
    private String email;

    /** 휴대폰 번호입니다. */
    @Size(max = 30, message = "휴대폰 번호는 30자 이하여야 합니다.")
    private String mobileNo;

    /** 회원 상태입니다. */
    private String memberStatus;

    /** 잠금 여부입니다. */
    private String lockYn;

    /** 탈퇴 여부입니다. */
    private String withdrawYn;

    /** 가입 또는 유입 채널 코드입니다. */
    private String channelCode;

    /** 가입 일시입니다. */
    private LocalDateTime joinedAt;

    /** 최근 로그인 일시입니다. */
    private LocalDateTime lastLoginAt;

    /** 회원 설명 또는 운영 메모입니다. */
    @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
    private String description;

    /** 등록자 ID입니다. */
    private String createdBy;

    /** 등록 일시입니다. */
    private LocalDateTime createdAt;

    /** 수정자 ID입니다. */
    private String updatedBy;

    /** 수정 일시입니다. */
    private LocalDateTime updatedAt;

    /** 회원 생성 요청 검증 그룹입니다. */
    public interface CreateGroup {
    }

    /** 회원 수정 요청 검증 그룹입니다. */
    public interface UpdateGroup {
    }
}
