package com.cpf.web.context;

/** Values resolved by the trusted ingress/runtime boundary, never directly trusted from arbitrary client headers. */
public record CpfHttpIngressMetadata(
        String ingressChannel,
        String edgeId,
        String verifiedCallerSystemCode,
        String clientIp,
        String countryCode,
        String apiVersion,
        String currentChannel) {

    /** Compatibility constructor for call sites that have no trusted ingress Channel. */
    public CpfHttpIngressMetadata(String edgeId, String verifiedCallerSystemCode,
            String clientIp, String countryCode, String apiVersion, String currentChannel) {
        this(null, edgeId, verifiedCallerSystemCode, clientIp, countryCode, apiVersion, currentChannel);
    }
}
