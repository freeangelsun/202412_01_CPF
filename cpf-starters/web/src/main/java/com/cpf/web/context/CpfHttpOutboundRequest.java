package com.cpf.web.context;

import java.util.Map;

/** Describes a concrete outbound HTTP target and trust boundary. */
public record CpfHttpOutboundRequest(
        String targetChannel,
        String targetOperation,
        String apiVersion,
        boolean trustedInternal,
        Map<String,String> customHeaders) {
    public CpfHttpOutboundRequest {
        customHeaders = customHeaders == null ? Map.of() : Map.copyOf(customHeaders);
    }
    public CpfHttpOutboundRequest(String targetChannel, String targetOperation, String apiVersion, boolean trustedInternal) {
        this(targetChannel, targetOperation, apiVersion, trustedInternal, Map.of());
    }
    public CpfHttpOutboundRequest(String targetChannel, String targetOperation, String apiVersion) {
        this(targetChannel, targetOperation, apiVersion, false, Map.of());
    }
}
