package com.cpf.admin.opr.filejob;

/** ADM File Job 행별 처리 결과의 공개 조회 DTO입니다. Payload와 Rollback Token은 노출하지 않습니다. */
public record AdmFileJobRowResponse(
        long rowNumber, String state, String businessKey, String errorCode, String message) { }
