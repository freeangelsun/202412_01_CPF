package com.cpf.notification.api;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

/** Provider delivery receipt normalized by CPF. */
/** CpfNotificationReceipt 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfNotificationReceipt(
        String receiptId, String notificationId, String provider,
        String receiptStatus, String detail, Instant receivedAt) {
    private static final Set<String> ALLOWED=Set.of("ACCEPTED","DELIVERED","BOUNCED","REJECTED","UNKNOWN");
    public CpfNotificationReceipt {
        receiptId=required(receiptId,"receiptId");notificationId=required(notificationId,"notificationId");provider=required(provider,"provider");
        receiptStatus=required(receiptStatus,"receiptStatus").toUpperCase(Locale.ROOT);if(!ALLOWED.contains(receiptStatus))throw new IllegalArgumentException("unsupported receipt status: "+receiptStatus);
        detail=detail==null||detail.isBlank()?null:detail.trim();if(receivedAt==null)throw new IllegalArgumentException("receivedAt is required");
    }
    private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
}
