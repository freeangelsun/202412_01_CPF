package com.cpf.batch.edu.servicecall;

import java.util.Objects;

/**
 * BAT가 배치 처리 중 typed client를 사용하는 기본 교육 샘플입니다.
 */
public class BatServiceCallEngineEducationSample {
    private final BatMemberGradeClient memberGradeClient;

    /** typed client를 주입합니다. */
    public BatServiceCallEngineEducationSample(BatMemberGradeClient memberGradeClient) {
        this.memberGradeClient = Objects.requireNonNull(memberGradeClient, "memberGradeClient는 필수입니다.");
    }

    /** 제품 기본 정책으로 회원등급을 조회합니다. */
    public BatMemberGradeResponse callMemberGrade(String memberNo) {
        return memberGradeClient.execute(new BatMemberGradeRequest(memberNo));
    }
}
