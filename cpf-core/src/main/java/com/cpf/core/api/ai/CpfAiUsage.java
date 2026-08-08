package com.cpf.core.api.ai;
/** Provider가 반환한 usage/cost snapshot입니다. 비용은 통화 최소단위 또는 내부 cost unit입니다. */
public record CpfAiUsage(long inputTokens,long outputTokens,long costUnits) { public CpfAiUsage { if(inputTokens<0||outputTokens<0||costUnits<0) throw new IllegalArgumentException("usage must not be negative"); } }
