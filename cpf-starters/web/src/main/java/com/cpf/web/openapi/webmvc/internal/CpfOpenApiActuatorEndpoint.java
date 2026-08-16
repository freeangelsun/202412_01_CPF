package com.cpf.web.openapi.webmvc.internal;

import com.cpf.web.api.openapi.CpfOpenApiOperations;
import com.cpf.web.api.openapi.CpfOpenApiSnapshot;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id = "cpfOpenApi")
final class CpfOpenApiActuatorEndpoint {
    private final CpfOpenApiOperations operations;
    CpfOpenApiActuatorEndpoint(CpfOpenApiOperations operations) { this.operations = operations; }
    @ReadOperation CpfOpenApiSnapshot snapshot() { return operations.snapshot(); }
}
