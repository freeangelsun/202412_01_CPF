package com.cpf.education.online.webhook.service;
import com.cpf.core.api.error.*; import com.cpf.education.online.webhook.dto.PaymentWebhookRequest; import com.cpf.education.online.webhook.security.PaymentWebhookSignatureVerifier; import com.cpf.foundation.annotation.CpfService; import com.cpf.integration.api.webhook.*; import java.nio.charset.StandardCharsets; import java.time.Instant;
@CpfService
/** PaymentWebhookService는 Webhook 인증·멱등성·중복 Callback·최종 Reconcile을 처리하는 Golden Path입니다. */
public class PaymentWebhookService { private final CpfWebhookOperations webhooks; private final PaymentWebhookSignatureVerifier verifier; public PaymentWebhookService(CpfWebhookOperations webhooks,PaymentWebhookSignatureVerifier verifier){this.webhooks=webhooks;this.verifier=verifier;}
 /** accept 동작은 Webhook 인증·멱등성·중복 Callback·최종 Reconcile을 처리하는 Golden Path에서 필요한 공개 동작을 수행합니다. */
 public CpfWebhookDelivery accept(PaymentWebhookRequest r){if(!verifier.verify(r)) throw new CpfBusinessException(CpfErrorCode.FORBIDDEN,"Callback signature verification failed"); return webhooks.enqueue(r.endpointId(),r.eventId(),r.eventType(),r.payload().getBytes(StandardCharsets.UTF_8),r.idempotencyKey(),Instant.now());} }
