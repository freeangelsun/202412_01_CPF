package com.cpf.core.spi.state;

import com.cpf.core.api.state.CpfStateSearchRequest;
import com.cpf.core.api.state.CpfStateSnapshot;
import java.util.List;
import java.util.Optional;

/** Provider SPI for atomic version and operation-id compare-and-set semantics. */
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

    record SearchResult(SearchStatus status, List<CpfStateSnapshot> items, String nextCursor) {
        public SearchResult {
            if (status == null) throw new IllegalArgumentException("status is required");
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    enum SearchStatus { SUCCESS, UNSUPPORTED, UNKNOWN }
}
