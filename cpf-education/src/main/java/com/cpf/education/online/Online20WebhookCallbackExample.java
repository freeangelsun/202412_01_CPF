package com.cpf.education.online;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.integration.api.webhook.CpfWebhookDelivery;
import com.cpf.integration.api.webhook.CpfWebhookOperations;
import com.cpf.security.api.crypto.CpfDigitalSignature;
import com.cpf.security.api.crypto.CpfDigitalSignatureOperations;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인-20 Callback·Webhook 결과: 서명검증과 idempotency key로 duplicate/late callback을 동일 상태로 수렴시킵니다. */
@CpfRestController
@RequestMapping("/edu/online/20-webhook")
public class Online20WebhookCallbackExample {
    private final CpfWebhookOperations webhooks;
    private final CpfDigitalSignatureOperations signatures;

    public Online20WebhookCallbackExample(
            CpfWebhookOperations webhooks,
            CpfDigitalSignatureOperations signatures) {
        this.webhooks = webhooks;
        this.signatures = signatures;
    }

    @PostMapping
    @Operation(operationId = "EDU-ONLINE-20", summary = "Callback·Webhook 비동기 결과 거래")
    @CpfOnlineTransaction(
            operationId = "EDU-ONLINE-20",
            name = "Callback·Webhook 비동기 결과 거래",
            description = "CPF Signature 검증 후 Webhook idempotency key로 duplicate/late callback을 수렴시키고 최초 transaction과 correlation한다.")
    /** callback 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public CpfWebhookDelivery callback(@RequestBody CallbackCommand command) {
        byte[] payload = command.payload().getBytes(StandardCharsets.UTF_8);
        CpfDigitalSignature signature = new CpfDigitalSignature(
                command.keyId(), command.keyVersion(), command.algorithm(), command.certificateId(), command.signature(), command.signedAt());
        if (!signatures.verify(CpfContexts.transactionId(), payload, signature)) {
            throw new CpfBusinessException(CpfErrorCode.FORBIDDEN, "Callback signature verification failed");
        }
        return webhooks.enqueue(
                command.endpointId(),
                command.eventId(),
                command.eventType(),
                payload,
                command.idempotencyKey(),
                Instant.now());
    }

    /** 이 타입은 해당 EDU 시나리오의 입력·출력 또는 업무 경계를 명확히 표현합니다. */
    public record CallbackCommand(
            String endpointId,
            String eventId,
            String eventType,
            String payload,
            String idempotencyKey,
            String keyId,
            String keyVersion,
            String algorithm,
            String certificateId,
            byte[] signature,
            Instant signedAt) { }
}
