package com.cpf.common.parameter.api;

import java.util.List;
import java.util.Map;

/** Parameter Reference Picker가 사용하는 topology-independent 검색 Port입니다. */
public interface CpfParameterReferenceCatalogPort {
    CatalogPage search(ReferenceQuery query);

    record ReferenceQuery(
            String referenceType, String parentType, String parentId,
            String query, int offset, int limit, String actorId) {}
    record ReferenceItem(
            String id, String label, String referenceType, String parentId,
            boolean enabled, String disabledReason, Map<String,Object> metadata) {}
    record CatalogPage(
            String referenceType, boolean installed, boolean available, String unavailableReason,
            int offset, int limit, boolean hasNext, List<ReferenceItem> items) {}
}
