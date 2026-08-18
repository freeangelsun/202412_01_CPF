package com.cpf.education.online.webhook.recovery;
import com.cpf.integration.api.webhook.CpfWebhookOperations; import java.time.Instant;
/** missing/late callback window 이후 CPF Webhook reconcile Runtime을 호출하는 업무 Recovery 경계입니다. */
public final class PaymentWebhookReconcileService { private final CpfWebhookOperations webhooks; public PaymentWebhookReconcileService(CpfWebhookOperations webhooks){this.webhooks=webhooks;} public CpfWebhookOperations.ReconcileResult reconcile(CpfWebhookOperations.Transport transport){return webhooks.reconcile(Instant.now(),transport);} }
