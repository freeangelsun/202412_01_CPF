package cpf.bat.edu.servicecall;

import cpf.pfw.common.base.CpfQuery;

/** 배치 회원등급 조회 입력입니다. */
public record BatMemberGradeRequest(String memberNo) implements CpfQuery {
    /** 입력값을 검증합니다. */
    public BatMemberGradeRequest {
        if (memberNo == null || memberNo.isBlank()) {
            throw new IllegalArgumentException("memberNo는 필수입니다.");
        }
        memberNo = memberNo.trim();
    }
}
