package com.cpf.core.api.webhook;
import java.time.Instant;
public record CpfWebhookDelivery(String deliveryId,String endpointId,String eventId,String eventType,String idempotencyKey,long sequence,State state,int attempts,Instant nextAttemptAt,String lastError,long version){
 public enum State{PENDING,DELIVERING,DELIVERED,RETRY,DLQ,UNKNOWN,CANCELLED}
}
