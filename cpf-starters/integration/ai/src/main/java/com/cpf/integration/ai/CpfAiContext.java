package com.cpf.integration.ai;

import java.util.Locale;

/**
 * AI capability가 자체 소유하는 감사/관측용 메타데이터입니다.
 * Core Context 확장 Component가 아니며 credential/prompt 원문을 절대 보관하지 않습니다.
 */
public record CpfAiContext(
        String operationId,
        String modelAlias,
        String providerAlias,
        String promptTemplateId,
        String safetyPolicyId,
        int attempt) {
    public CpfAiContext {
        rejectCredential(operationId);
        rejectCredential(modelAlias);
        rejectCredential(providerAlias);
        rejectCredential(promptTemplateId);
        rejectCredential(safetyPolicyId);
        if (attempt < 1) throw new IllegalArgumentException("attempt");
    }

    private static void rejectCredential(String value) {
        if (value == null) return;
        String lower = value.toLowerCase(Locale.ROOT);
        for (String token : new String[]{"api_key=", "apikey=", "secret=", "password=", "bearer "}) {
            if (lower.contains(token)) {
                throw new IllegalArgumentException("credential material is forbidden in AI context metadata");
            }
        }
    }
}
