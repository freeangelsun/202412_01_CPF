package cpf.adm.edu.operation;

import java.util.Map;

/**
 * ADM 운영 콘솔에서 거래/배치/브로커 상태를 조회할 때 사용할 검색 조건 샘플입니다.
 */
public class AdmOperationEducationSample {

    public Map<String, String> transactionLogQuery(String transactionGlobalId) {
        return Map.of(
                "transactionGlobalId", transactionGlobalId,
                "tabs", "summary,headers,request,response,error,diff",
                "masking", "enabled");
    }
}
