package com.cpf.core.api.notification;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

/** Provider delivery receipt normalized by CPF. */
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
