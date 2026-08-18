package com.cpf.education.online.webhook.dto;
import java.time.Instant;
/** PaymentWebhookRequest는 Webhook 인증·멱등성·중복 Callback·최종 Reconcile을 처리하는 Golden Path입니다. */
public record PaymentWebhookRequest(String endpointId,String eventId,String eventType,String payload,String idempotencyKey,String keyId,String keyVersion,String algorithm,String certificateId,byte[] signature,Instant signedAt) { }
