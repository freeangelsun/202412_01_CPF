package com.cpf.web.context;

import java.util.Map;

/** Describes a concrete outbound HTTP target and trust boundary. */
public record CpfHttpOutboundRequest(
        String targetSystemCode,
        String targetChannel,
        String targetOperation,
        String apiVersion,
        boolean trustedInternal,
        Map<String,String> customHeaders) {
    public CpfHttpOutboundRequest {
        customHeaders = customHeaders == null ? Map.of() : Map.copyOf(customHeaders);
    }

    /** Compatibility constructor: generated-domain callers historically used one value for System and Channel. */
    public CpfHttpOutboundRequest(String targetSystemCode, String targetOperation, String apiVersion,
                                  boolean trustedInternal, Map<String,String> customHeaders) {
        this(targetSystemCode, targetSystemCode, targetOperation, apiVersion, trustedInternal, customHeaders);
    }

    public CpfHttpOutboundRequest(String targetSystemCode, String targetOperation, String apiVersion, boolean trustedInternal) {
        this(targetSystemCode, targetSystemCode, targetOperation, apiVersion, trustedInternal, Map.of());
    }

    public CpfHttpOutboundRequest(String targetSystemCode, String targetOperation, String apiVersion) {
        this(targetSystemCode, targetSystemCode, targetOperation, apiVersion, false, Map.of());
    }
}
