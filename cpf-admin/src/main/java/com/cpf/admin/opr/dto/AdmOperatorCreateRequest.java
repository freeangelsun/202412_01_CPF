package com.cpf.admin.opr.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * ADM 운영자 생성 요청입니다.
 *
 * <p>mobileNo/officePhoneNo는 인증 Identity가 아니라 운영자 Directory/Profile에 저장됩니다.</p>
 */
@Schema(description = "ADM 운영자 생성 요청. operationId는 결과불명 재시도에서 동일 값을 재사용합니다.")
public record AdmOperatorCreateRequest(
        @Schema(example = "adm001") String operatorId,
        @Schema(example = "운영자 홍길동") String operatorName,
        @Schema(example = "010-1234-5678") String mobileNo,
        @Schema(example = "02-1234-5678") String officePhoneNo,
        @Schema(description = "멱등 생성 Operation ID", example = "550e8400-e29b-41d4-a716-446655440000") String operationId,
        @Schema(description = "비밀번호 정책을 만족하는 초기 비밀번호", example = "ChangeMe#2026") String password,
        @Schema(hidden = true) List<String> roleIds,
        @Schema(example = "admin") String requestUser,
        @Schema(description = "감사 사유", example = "신규 운영 담당자 등록") String reason) {

    /** operationId 도입 전 연락처 Consumer 호환 생성자입니다. */
    public AdmOperatorCreateRequest(
            String operatorId, String operatorName, String mobileNo, String officePhoneNo,
            String password, List<String> roleIds, String requestUser, String reason) {
        this(operatorId, operatorName, mobileNo, officePhoneNo, null, password, roleIds, requestUser, reason);
    }

    /** 연락처 필드 추가 전 Consumer의 생성자 계약을 유지합니다. */
    public AdmOperatorCreateRequest(
            String operatorId,
            String operatorName,
            String password,
            List<String> roleIds,
            String requestUser,
            String reason) {
        this(operatorId, operatorName, null, null, null, password, roleIds, requestUser, reason);
    }
}
