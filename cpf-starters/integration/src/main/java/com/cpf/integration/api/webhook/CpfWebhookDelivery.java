package com.cpf.integration.api.webhook;
import java.time.Instant;
/** CpfWebhookDelivery 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfWebhookDelivery(String deliveryId,String endpointId,String eventId,String eventType,String idempotencyKey,long sequence,State state,int attempts,Instant nextAttemptAt,String lastError,long version){
 public enum State{PENDING,DELIVERING,DELIVERED,RETRY,DLQ,UNKNOWN,CANCELLED}
}
