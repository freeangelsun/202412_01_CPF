package {{PACKAGE_NAME}}.integration.webhook;
import com.cpf.core.api.webhook.CpfWebhookOperations;
/** Generated domain adapter: customer code supplies event mapping only; signing/retry/DLQ remain CPF-owned. */
public final class {{DOMAIN_NAME}}WebhookAdapter {
 private final CpfWebhookOperations operations;
 public {{DOMAIN_NAME}}WebhookAdapter(CpfWebhookOperations operations){this.operations=operations;}
 public void publish(String endpointId,String eventId,String eventType,byte[] payload,String idempotencyKey,java.time.Instant now){operations.enqueue(endpointId,eventId,eventType,payload,idempotencyKey,now);}
}
