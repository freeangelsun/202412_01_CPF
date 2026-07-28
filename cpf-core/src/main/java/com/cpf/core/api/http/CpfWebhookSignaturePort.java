package com.cpf.core.api.http;
/** Webhook payload 서명을 외부 KMS/HSM/Secret Adapter에 위임합니다. */
public interface CpfWebhookSignaturePort { String sign(String signatureRef,String operationId,Object payload); }
