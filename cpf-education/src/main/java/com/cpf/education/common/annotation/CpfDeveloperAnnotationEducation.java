package com.cpf.education.common.annotation;
import java.util.LinkedHashMap;
import java.util.Map;

/** CPF 개발자가 Golden Path에서 사용하는 18개 Canonical Developer Annotation Owner 안내입니다. */
public final class CpfDeveloperAnnotationEducation {
    private CpfDeveloperAnnotationEducation() { }
    public static Map<String, String> canonicalOwners() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("CpfController", "web-api"); values.put("CpfService", "base");
        values.put("CpfRepository", "data-persistence");
        values.put("CpfDto", "data"); values.put("CpfBatchJob", "batch"); values.put("CpfBatchStep", "batch");
        values.put("CpfMessageListener", "messaging"); values.put("CpfClient", "integration");
        values.put("CpfTx", "data-persistence"); values.put("CpfIdempotent", "reliability/base");
        values.put("CpfRetry", "integration"); values.put("CpfTimeout", "integration");
        values.put("CpfLogging", "base"); values.put("CpfPerformance", "base");
        values.put("CpfAudit", "platform-operations"); values.put("CpfPermission", "security");
        values.put("CpfApprovalRequired", "security");
        return Map.copyOf(values);
    }
}
