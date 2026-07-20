package cpf.xyz.servicecall;

import cpf.pfw.common.base.CpfQuery;

/**
 * MBR 회원 요약 조회에 필요한 업무 입력입니다.
 *
 * @param memberNo 회원번호
 */
public record XyzMemberSummaryRequest(String memberNo) implements CpfQuery {

    /** 입력값을 검증합니다. */
    public XyzMemberSummaryRequest {
        if (memberNo == null || memberNo.isBlank()) {
            throw new IllegalArgumentException("memberNo는 필수입니다.");
        }
        memberNo = memberNo.trim();
    }
}
