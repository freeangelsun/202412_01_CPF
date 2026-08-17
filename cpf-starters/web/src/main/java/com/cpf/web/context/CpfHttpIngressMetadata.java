package com.cpf.web.context;

/** Values resolved by the trusted ingress/runtime boundary, never directly trusted from arbitrary client headers. */
public record CpfHttpIngressMetadata(
        String edgeId,
        String callerApplication,
        String clientIp,
        String countryCode,
        String apiVersion,
        String currentSystemCode) {
    /** Compatibility constructor for older web filter call sites. */
    public CpfHttpIngressMetadata(String legacyChannelCode, String edgeId, String callerApplication,
            String clientIp, String countryCode, String apiVersion) {
        this(edgeId, callerApplication, clientIp, countryCode, apiVersion, null);
    }
}
