package com.cpf.core.api.notification;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

/** Provider-neutral result with explicit UNKNOWN_RESULT support. */
public record CpfNotificationResult(
        String notificationId,
        String provider,
        String status,
        String providerMessageId,
        String detail,
        Instant processedAt) {
    private static final Set<String> ALLOWED = Set.of("ACCEPTED","SENT","DELIVERED","FAILED","REJECTED","PENDING","CLAIMED","RECONCILING","DLQ","UNKNOWN_RESULT");
    public CpfNotificationResult {
        notificationId = required(notificationId, "notificationId");
        status = required(status, "status").toUpperCase(Locale.ROOT);
        if (!ALLOWED.contains(status)) throw new IllegalArgumentException("unsupported notification status: " + status);
        provider = optional(provider); providerMessageId = optional(providerMessageId); detail = optional(detail);
        if ((status.equals("SENT") || status.equals("DELIVERED") || status.equals("UNKNOWN_RESULT")) && provider == null) {
            throw new IllegalArgumentException("provider is required for status " + status);
        }
        processedAt = processedAt == null ? Instant.now() : processedAt;
    }
    public static CpfNotificationResult sent(String id,String provider,String messageId){return new CpfNotificationResult(id,provider,"SENT",messageId,null,Instant.now());}
    public static CpfNotificationResult unknown(String id,String provider,String detail){return new CpfNotificationResult(id,provider,"UNKNOWN_RESULT",null,detail,Instant.now());}
    private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
    private static String optional(String v){return v==null||v.isBlank()?null:v.trim();}
}
