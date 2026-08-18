package com.cpf.core.api.tracking;

import java.util.Objects;

/**
 * Boundary에서 일시적으로 전달되는 Subject 후보입니다.
 *
 * <p>{@code subjectId}는 검색 Token 생성 직전까지만 메모리에 존재하는 원문 값이며 일반 로그/Audit/Evidence에
 * 출력하면 안 됩니다. Type/Role/Source/Trust가 명시되지 않은 값은 자동 추적 대상으로 인정하지 않습니다.</p>
 */
public record CpfSubjectCandidate(
        CpfSubjectType subjectType,
        CpfSubjectRole subjectRole,
        String subjectId,
        CpfSubjectSourceType sourceType,
        CpfSubjectTrustLevel trustLevel) {

    public CpfSubjectCandidate {
        subjectType = Objects.requireNonNull(subjectType, "subjectType");
        subjectRole = Objects.requireNonNull(subjectRole, "subjectRole");
        sourceType = Objects.requireNonNull(sourceType, "sourceType");
        trustLevel = Objects.requireNonNull(trustLevel, "trustLevel");
        if (subjectId == null || subjectId.isBlank()) throw new IllegalArgumentException("subjectId is required");
        if (!subjectId.equals(subjectId.strip())) throw new IllegalArgumentException("subjectId must not contain surrounding whitespace");
        if (subjectId.length() > 256) throw new IllegalArgumentException("subjectId exceeds 256 characters");
    }
}
