package com.cpf.reference.servicecall;

import java.util.Objects;

/**
 * REF가 typed client로 타 주제영역을 호출하는 기본 교육 샘플입니다.
 *
 * <p>업무 개발자는 회원번호만 전달합니다. 레지스트리, URI, timeout, retry, failover,
 * circuit과 segment 기록은 CPF 및 remote adapter가 처리합니다.</p>
 */
public class ReferenceServiceCallEngineEducationSample {
    private final ReferenceMemberSummaryClient memberSummaryClient;

    /**
     * 교육 샘플을 생성합니다.
     *
     * @param memberSummaryClient local/remote 공통 typed client
     */
    public ReferenceServiceCallEngineEducationSample(ReferenceMemberSummaryClient memberSummaryClient) {
        this.memberSummaryClient = Objects.requireNonNull(memberSummaryClient, "memberSummaryClient는 필수입니다.");
    }

    /**
     * 제품 기본 정책으로 회원 요약을 조회합니다.
     *
     * @param memberNo 회원번호
     * @return typed 회원 요약
     */
    public ReferenceMemberSummaryResponse callMemberSummary(String memberNo) {
        return memberSummaryClient.execute(new ReferenceMemberSummaryRequest(memberNo));
    }
}
