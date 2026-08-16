package com.cpf.integration.realtime;

/** Realtime capability가 자체 소유하는 subscription 메타데이터입니다. */
public record CpfRealtimeContext(
        String subscriptionId,
        String channel,
        String topic,
        String subjectId,
        String tenantId,
        long replayCursor,
        int attempt) {
    public CpfRealtimeContext {
        if (replayCursor < 0) throw new IllegalArgumentException("replayCursor");
        if (attempt < 1) throw new IllegalArgumentException("attempt");
    }
}
