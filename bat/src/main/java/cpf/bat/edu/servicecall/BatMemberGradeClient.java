package cpf.bat.edu.servicecall;

import cpf.pfw.common.servicecall.CpfTypedServiceClient;

/** BAT가 MBR 회원등급을 조회할 때 사용하는 typed client입니다. */
public interface BatMemberGradeClient
        extends CpfTypedServiceClient<BatMemberGradeRequest, BatMemberGradeResponse> {
}
