package com.cpf.platform.operations.spi.state;

import com.cpf.platform.operations.api.state.CpfStateSearchRequest;
import com.cpf.platform.operations.api.state.CpfStateSnapshot;
import java.util.List;
import java.util.Optional;

/** Provider SPI for atomic version and operation-id compare-and-set semantics. */
/** CpfStateStore 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfStateStore {
    Optional<CpfStateSnapshot> find(String stateKey);

    WriteResult compareAndSet(
            String stateKey,
            long expectedVersion,
            String operationId,
            String commandHash,
            CpfStateSnapshot next);

    default SearchResult search(CpfStateSearchRequest request) {
        return new SearchResult(SearchStatus.UNSUPPORTED, List.of(), null);
    }

    /** WriteResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record WriteResult(Status status, CpfStateSnapshot snapshot) {
        public WriteResult {
            if (status == null) throw new IllegalArgumentException("status is required");
        }
    }

    enum Status {
        APPLIED,
        IDEMPOTENT_REPLAY,
        CONFLICT,
        OPERATION_CONFLICT,
        RESOURCE_EXHAUSTED,
        UNKNOWN
    }

    /** SearchResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record SearchResult(SearchStatus status, List<CpfStateSnapshot> items, String nextCursor) {
        public SearchResult {
            if (status == null) throw new IllegalArgumentException("status is required");
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    enum SearchStatus { SUCCESS, UNSUPPORTED, UNKNOWN }
}
