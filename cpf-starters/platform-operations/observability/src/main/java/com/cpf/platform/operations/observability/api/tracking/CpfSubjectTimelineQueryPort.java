package com.cpf.platform.operations.observability.api.tracking;

import com.cpf.core.api.tracking.CpfSubjectRole;
import com.cpf.core.api.tracking.CpfSubjectType;
import com.cpf.core.api.tracking.CpfSubjectTrustLevel;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Subject protected search key를 transactionId 목록으로 해석하는 운영 조회 Port입니다. */
public interface CpfSubjectTimelineQueryPort {
    SearchResult findTransactions(SearchRequest request);

    /** 보호 검색 토큰으로 변환할 Subject와 조회 기간·기본 신뢰수준을 전달하는 검색 요청입니다. */
    record SearchRequest(
            CpfSubjectType subjectType,
            CpfSubjectRole subjectRole,
            String subjectId,
            Instant from,
            Instant to,
            int limit,
            CpfSubjectTrustLevel minimumTrust) {
        /** Subject 검색 입력을 정규화하고 기본 Role/Trust/Limit을 적용해 잘못된 조회를 fail-fast합니다. */
        public SearchRequest {
            if (subjectType == null) throw new IllegalArgumentException("subjectType is required");
            subjectRole = subjectRole == null ? CpfSubjectRole.ACTOR : subjectRole;
            if (subjectId == null || subjectId.isBlank()) throw new IllegalArgumentException("subjectId is required");
            if (!subjectId.equals(subjectId.strip())) throw new IllegalArgumentException("subjectId must not contain surrounding whitespace");
            if (from != null && to != null && from.isAfter(to)) throw new IllegalArgumentException("from must not be after to");
            limit = Math.max(1, Math.min(500, limit <= 0 ? 100 : limit));
            minimumTrust = minimumTrust == null ? CpfSubjectTrustLevel.TRUSTED : minimumTrust;
        }

        /** 기본 TRUSTED 신뢰수준으로 Subject Timeline을 조회하는 편의 생성자입니다. */
        public SearchRequest(CpfSubjectType subjectType, CpfSubjectRole subjectRole, String subjectId,
                Instant from, Instant to, int limit) {
            this(subjectType, subjectRole, subjectId, from, to, limit, CpfSubjectTrustLevel.TRUSTED);
        }
    }

    record SearchResult(
            boolean available,
            String maskedSubject,
            List<Map<String, Object>> items,
            int limit,
            String message) {
        public SearchResult {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }
}
