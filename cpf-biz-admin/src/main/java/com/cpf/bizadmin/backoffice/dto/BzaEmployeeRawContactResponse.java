package com.cpf.bizadmin.backoffice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 감사 완료 후에만 클라이언트로 반환되는 직원 연락처 원문 최소 응답입니다.
 * DB에서는 연락처 4개 필드만 Projection하며 일반 직원 Row 전체를 Raw 경로로 적재하지 않습니다.
 * PII_RAW 권한/사유/감사 중 하나라도 실패하면 원문을 반환하지 않고, Browser는 close·route·logout·재조회 실패 시
 * 기존 값을 즉시 zeroization해야 합니다. 이 계약은 다중 인스턴스에서도 공용 DB 감사 정본을 기준으로 합니다.
 */
@Schema(description = "감사 완료된 BZA 직원 연락처 원문 최소 응답")
public record BzaEmployeeRawContactResponse(
        @Schema(description = "직원번호") String employeeNo,
        @Schema(description = "이메일 원문") String email,
        @Schema(description = "휴대전화 원문") String mobileNo,
        @Schema(description = "사무실 전화 원문") String officePhoneNo,
        @Schema(description = "원문 조회 허용 여부") boolean rawViewAllowed,
        @Schema(description = "CPF 거래 추적 ID") String transactionId) {
}
