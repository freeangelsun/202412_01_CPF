package com.cpf.web.context;

/** Describes the outbound HTTP trust boundary. */
public record CpfHttpOutboundRequest(
        String targetSystem,
        String targetOperation,
        String apiVersion,
        boolean trustedInternal) {
    public CpfHttpOutboundRequest(String targetSystem, String targetOperation, String apiVersion) {
        this(targetSystem, targetOperation, apiVersion, false);
    }
}
