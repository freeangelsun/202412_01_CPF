package com.cpf.education.online.webhook.controller;
import com.cpf.education.online.webhook.dto.PaymentWebhookRequest; import com.cpf.education.online.webhook.service.PaymentWebhookService; import com.cpf.foundation.execution.api.CpfOnlineTransaction; import com.cpf.integration.api.webhook.CpfWebhookDelivery; import com.cpf.web.api.CpfRestController; import io.swagger.v3.oas.annotations.Operation; import org.springframework.web.bind.annotation.*;
@CpfRestController @RequestMapping("/edu/online/payment-webhook")
/** PaymentWebhookController는 Webhook 인증·멱등성·중복 Callback·최종 Reconcile을 처리하는 Golden Path입니다. */
public class PaymentWebhookController { private final PaymentWebhookService service; public PaymentWebhookController(PaymentWebhookService service){this.service=service;}
 @PostMapping @Operation(operationId="EDU_PAYMENT_WEBHOOK",summary="Webhook callback") @CpfOnlineTransaction(operationId="EDU_PAYMENT_WEBHOOK",name="결제 Webhook",description="Signature→Idempotency→Webhook Runtime→Reconcile 책임을 분리한다.") public CpfWebhookDelivery callback(@RequestBody PaymentWebhookRequest r){return service.accept(r);} }
