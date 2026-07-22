package com.cpf.batch.edu.servicecall;

import com.cpf.core.common.servicecall.CpfTypedServiceClient;

/** BAT가 MBR 회원등급을 조회할 때 사용하는 typed client입니다. */
public interface BatMemberGradeClient
        extends CpfTypedServiceClient<BatMemberGradeRequest, BatMemberGradeResponse> {
}
