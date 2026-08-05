package com.cpf.core.api.state;

import java.util.List;

/** Typed search result that does not collapse provider failure into an empty page. */
public record CpfStateSearchResult(Status status, List<CpfStateSnapshot> items, String nextCursor) {
    public CpfStateSearchResult {
        if (status == null) throw new IllegalArgumentException("status is required");
        items = items == null ? List.of() : List.copyOf(items);
        if (status != Status.SUCCESS && (!items.isEmpty() || nextCursor != null)) {
            throw new IllegalArgumentException("failed search must not contain page data");
        }
    }

    public enum Status { SUCCESS, STORE_UNAVAILABLE, UNSUPPORTED }
}
