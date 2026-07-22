package com.cpf.batch.edu.servicecall;

import com.cpf.core.common.base.CpfResponse;

/** 배치 회원등급 조회 응답입니다. */
public record BatMemberGradeResponse(String memberNo, String gradeCode) implements CpfResponse {
}
