package cpf.adm.opr.dto;

import java.util.List;

/**
 * ADM operator creation request.
 */
public record AdmOperatorCreateRequest(
        String operatorId,
        String operatorName,
        String password,
        List<String> roleIds,
        String requestUser,
        String reason) {
}
