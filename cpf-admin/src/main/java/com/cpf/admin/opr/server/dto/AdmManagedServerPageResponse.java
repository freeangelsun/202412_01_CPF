package com.cpf.admin.opr.server.dto;

import com.cpf.platform.operations.runtimecontrol.CpfManagedServerPage;

import java.util.List;

public record AdmManagedServerPageResponse(
        List<AdmManagedServerResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {
    public AdmManagedServerPageResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static AdmManagedServerPageResponse from(CpfManagedServerPage source) {
        return new AdmManagedServerPageResponse(source.items().stream().map(AdmManagedServerResponse::from).toList(),
                source.page(), source.size(), source.totalElements(), source.totalPages(), source.hasNext());
    }
}
