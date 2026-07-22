package com.cpf.reference.servicecall;

import com.cpf.core.common.base.CpfResponse;

/**
 * MBR 회원 요약 조회의 typed 응답입니다.
 *
 * @param memberNo 회원번호
 * @param memberName 마스킹 또는 권한 정책이 적용된 회원명
 * @param statusCode 회원 상태 코드
 */
public record ReferenceMemberSummaryResponse(
        String memberNo,
        String memberName,
        String statusCode) implements CpfResponse {
}
