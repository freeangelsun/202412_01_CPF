package com.cpf.admin.opr.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ADM 운영자 연락처 원문 조회의 최소 응답 계약입니다.
 *
 * <p>일반 운영자 DTO와 분리하여 역할, 계정상태, 잠금/비밀번호 상태, 생성·수정시각 등
 * Raw 연락처 조회 목적과 무관한 운영정보가 함께 노출되지 않도록 합니다.</p>
 * <p><b>보안:</b> PII_RAW 권한·업무 사유·감사 영속화가 모두 성공한 뒤에만 생성됩니다. Controller는
 * no-store 응답을 사용하고 Browser는 Modal close/route/logout 시 즉시 상태를 제거해야 합니다.</p>
 * <p><b>실패/복구:</b> 권한 부족은 403, 동시 변경은 409, 감사/DB 장애는 503으로 fail-closed하며
 * 실패 시 이전 Raw 값을 재사용해서는 안 됩니다. 다중 인스턴스에서도 DB 감사 정본을 공유합니다.</p>
 */
@Schema(description = "권한과 감사가 완료된 경우에만 반환되는 운영자 연락처 원문 최소 응답")
public record AdmOperatorRawContactResponse(
        @Schema(example = "adm001") String operatorId,
        @Schema(example = "010-1234-5678") String mobileNo,
        @Schema(example = "02-1234-5678") String officePhoneNo,
        @Schema(example = "true") boolean rawViewAllowed,
        @Schema(description = "감사 추적에 사용하는 CPF transactionId") String transactionId) {
}
