package com.cpf.admin.opr.server.dto;

public record AdmManagedServerDisableRequest(long expectedVersion, String reason) {
}
