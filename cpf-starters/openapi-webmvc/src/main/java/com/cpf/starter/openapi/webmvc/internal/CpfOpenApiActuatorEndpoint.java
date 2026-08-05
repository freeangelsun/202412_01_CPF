package com.cpf.starter.openapi.webmvc.internal;

import com.cpf.starter.openapi.webmvc.api.CpfOpenApiOperations;
import com.cpf.starter.openapi.webmvc.api.CpfOpenApiSnapshot;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id = "cpfOpenApi")
final class CpfOpenApiActuatorEndpoint {
    private final CpfOpenApiOperations operations;
    CpfOpenApiActuatorEndpoint(CpfOpenApiOperations operations) { this.operations = operations; }
    @ReadOperation CpfOpenApiSnapshot snapshot() { return operations.snapshot(); }
}
