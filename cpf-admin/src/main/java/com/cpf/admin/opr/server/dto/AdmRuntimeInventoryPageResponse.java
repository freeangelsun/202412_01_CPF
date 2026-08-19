package com.cpf.admin.opr.server.dto;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeInventoryPage;

import java.util.List;

public record AdmRuntimeInventoryPageResponse(
        List<AdmRuntimeInventoryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {
    public AdmRuntimeInventoryPageResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static AdmRuntimeInventoryPageResponse from(CpfRuntimeInventoryPage source) {
        return new AdmRuntimeInventoryPageResponse(source.items().stream().map(AdmRuntimeInventoryResponse::from).toList(),
                source.page(), source.size(), source.totalElements(), source.totalPages(), source.hasNext());
    }
}
