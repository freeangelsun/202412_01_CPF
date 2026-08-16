package com.cpf.education.web.header;
import java.util.Map;

/**
 * 온라인 거래 헤더 전파 샘플입니다.
 */
public class EducationHeaderPropagationEducationSample {

    public Map<String, String> propagate(String transactionId, String moduleId, String instanceId) {
        return Map.of(
                "x-cpf-transaction-id", transactionId,
                "x-cpf-module-id", moduleId,
                "x-cpf-instance-id", instanceId,
                "x-cpf-client-version", "edu-v1");
    }
}
