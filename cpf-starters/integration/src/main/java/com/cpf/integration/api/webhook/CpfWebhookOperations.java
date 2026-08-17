package com.cpf.integration.api.webhook;
import java.time.Instant; import java.util.*;
/** CpfWebhookOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfWebhookOperations {
 CpfWebhookEndpoint register(CpfWebhookEndpoint endpoint,String actor,String reason);
 CpfWebhookDelivery enqueue(String endpointId,String eventId,String eventType,byte[] payload,String idempotencyKey,Instant now);
 DeliveryAttempt deliverNext(String endpointId,Instant now,Transport transport);
 List<CpfWebhookDelivery> dlq(int limit);
 CpfWebhookDelivery replay(String deliveryId,long expectedVersion,String actor,String reason,Instant now);
 ReconcileResult reconcile(Instant now,Transport transport);
 interface Transport{TransportResult send(CpfWebhookEndpoint endpoint,Map<String,String> headers,byte[] payload);}
 record TransportResult(int statusCode,boolean resultUnknown,String error){ public boolean success(){return statusCode>=200&&statusCode<300&&!resultUnknown;} }
 /** DeliveryAttempt 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
 record DeliveryAttempt(CpfWebhookDelivery delivery,Map<String,String> signedHeaders,TransportResult result){}
 record ReconcileResult(int inspected,int delivered,int retried,int dlq,int unknown){}
}
