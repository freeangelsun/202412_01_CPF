package cpf.mbr.bse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MBR 회원 샘플 엔티티입니다.
 *
 * <p>개발 교육용 CRUD 샘플이면서 ADM 회원 관리 화면에서도 조회/변경할 수 있도록
 * 회원번호, 고객번호, 로그인 ID, 상태, 잠금, 탈퇴 여부를 함께 보관합니다.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 회원 내부 순번입니다. */
    private Integer id;

    /** 외부 식별에 사용하는 회원번호입니다. */
    private String memberNo;

    /** 고객 단위 식별이 필요한 경우 사용하는 고객번호입니다. */
    private String customerNo;

    /** 로그인 ID입니다. */
    private String loginId;

    /** 회원 비밀번호 hash입니다. */
    private String passwordHash;

    /** 로그인 실패 횟수입니다. */
    private Integer loginFailCount;

    /** 비밀번호 강제 변경 여부입니다. */
    private String passwordChangeRequiredYn;

    /** 비밀번호 만료 일시입니다. */
    private LocalDateTime passwordExpireAt;

    /** 회원명입니다. */
    private String name;

    /** 이메일 주소입니다. */
    private String email;

    /** 휴대폰 번호입니다. */
    private String mobileNo;

    /** 회원 상태입니다. 예: ACTIVE, SUSPENDED, DORMANT, WITHDRAWN */
    private String memberStatus;

    /** 계정 잠금 여부입니다. */
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
    private String description;

    /** 등록자 ID입니다. */
    private String createdBy;

    /** 등록 일시입니다. */
    private LocalDateTime createdAt;

    /** 수정자 ID입니다. */
    private String updatedBy;

    /** 수정 일시입니다. */
    private LocalDateTime updatedAt;
}
