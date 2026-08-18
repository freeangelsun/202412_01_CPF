package com.cpf.education.online.webhook.security;
import com.cpf.core.api.context.CpfContexts; import com.cpf.education.online.webhook.dto.PaymentWebhookRequest; import com.cpf.security.api.crypto.*; import java.nio.charset.StandardCharsets;
/** PaymentWebhookSignatureVerifier는 Webhook 인증·멱등성·중복 Callback·최종 Reconcile을 처리하는 Golden Path입니다. */
public final class PaymentWebhookSignatureVerifier { private final CpfDigitalSignatureOperations signatures; public PaymentWebhookSignatureVerifier(CpfDigitalSignatureOperations signatures){this.signatures=signatures;}
 /** verify 동작은 Webhook 인증·멱등성·중복 Callback·최종 Reconcile을 처리하는 Golden Path에서 필요한 공개 동작을 수행합니다. */
 public boolean verify(PaymentWebhookRequest r){return signatures.verify(CpfContexts.transactionId(),r.payload().getBytes(StandardCharsets.UTF_8),new CpfDigitalSignature(r.keyId(),r.keyVersion(),r.algorithm(),r.certificateId(),r.signature(),r.signedAt()));} }
