package cpf.adm.opr.dto;

/**
 * ADM 회원 등록/수정 요청입니다.
 *
 * @param memberNo     회원번호
 * @param customerNo   고객번호
 * @param loginId      로그인 ID
 * @param name         회원명
 * @param email        이메일
 * @param mobileNo     휴대폰 번호
 * @param memberStatus 회원 상태
 * @param lockYn       잠금 여부
 * @param withdrawYn   탈퇴 여부
 * @param channelCode  가입 또는 유입 채널
 * @param description  설명 또는 운영 메모
 * @param requestUser  요청자
 * @param reason       감사 사유
 */
public record AdmMemberSaveRequest(
        String memberNo,
        String customerNo,
        String loginId,
        String name,
        String email,
        String mobileNo,
        String memberStatus,
        String lockYn,
        String withdrawYn,
        String channelCode,
        String description,
        String requestUser,
        String reason) {
}
