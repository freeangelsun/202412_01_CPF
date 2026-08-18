package com.cpf.education.online.externalsideeffect.client;
import com.cpf.core.api.result.CpfResult; import com.cpf.education.online.externalsideeffect.dto.PaymentCommand; import com.cpf.integration.api.http.CpfRestClient; import java.util.Map;
/** PaymentInstitutionClient는 외부 Side Effect와 Local DB Commit 결과를 분리하고 UNKNOWN을 복구로 연결하는 거래 Golden Path입니다. */
public final class PaymentInstitutionClient {private final CpfRestClient rest;public PaymentInstitutionClient(CpfRestClient rest){this.rest=rest;}public CpfResult<String> pay(PaymentCommand c){return rest.exchangeResult("payment-agency","POST",b->b.path("/payments").build(),c,Map.of("X-Idempotency-Key",c.idempotencyKey()),String.class);}}
