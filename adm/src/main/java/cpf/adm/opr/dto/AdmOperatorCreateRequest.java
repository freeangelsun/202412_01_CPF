package cpf.adm.opr.dto;

import java.util.List;

/** ADM 운영자 생성 요청입니다. */
public record AdmOperatorCreateRequest(
        String operatorId,
        String operatorName,
        String password,
        List<String> roleIds,
        String requestUser,
        String reason) {
}
