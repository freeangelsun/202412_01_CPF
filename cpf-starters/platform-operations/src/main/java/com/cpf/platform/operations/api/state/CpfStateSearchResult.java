package com.cpf.platform.operations.api.state;

import java.util.List;

/** Typed search result that does not collapse provider failure into an empty page. */
/** CpfStateSearchResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfStateSearchResult(Status status, List<CpfStateSnapshot> items, String nextCursor) {
    public CpfStateSearchResult {
        if (status == null) throw new IllegalArgumentException("status is required");
        items = items == null ? List.of() : List.copyOf(items);
        if (status != Status.SUCCESS && (!items.isEmpty() || nextCursor != null)) {
            throw new IllegalArgumentException("failed search must not contain page data");
        }
    }

    /** Status 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum Status { SUCCESS, STORE_UNAVAILABLE, UNSUPPORTED }
}
