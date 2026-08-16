package com.cpf.notification.api;

import java.util.Locale;
import java.util.Set;

/** Sanitized Provider health result without transport SDK types. */
/** CpfNotificationProviderStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfNotificationProviderStatus(String status, String reasonCode) {
    private static final Set<String> ALLOWED=Set.of("UP","DOWN","DEGRADED","UNKNOWN");
    public CpfNotificationProviderStatus {
        status=required(status,"status").toUpperCase(Locale.ROOT);reasonCode=required(reasonCode,"reasonCode").toUpperCase(Locale.ROOT);
        if(!ALLOWED.contains(status))throw new IllegalArgumentException("unsupported provider status: "+status);
    }
    public static CpfNotificationProviderStatus up(){return new CpfNotificationProviderStatus("UP","AVAILABLE");}
    /** down 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfNotificationProviderStatus down(String reason){return new CpfNotificationProviderStatus("DOWN",reason);}
    public static CpfNotificationProviderStatus unknown(String reason){return new CpfNotificationProviderStatus("UNKNOWN",reason);}
    private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
}
