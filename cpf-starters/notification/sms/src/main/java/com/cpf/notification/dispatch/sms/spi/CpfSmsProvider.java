/** CpfSmsProvider 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
package com.cpf.notification.dispatch.sms.spi;public interface CpfSmsProvider {String providerName();SubmitResult submit(String recipient,String text,String idempotencyKey);record SubmitResult(String status,String providerMessageId,String detail){}}
