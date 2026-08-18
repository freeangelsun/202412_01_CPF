package com.cpf.education.online.recovery.client;
import com.cpf.core.api.result.CpfResult;
import com.cpf.education.online.recovery.dto.TransferCommand;
import com.cpf.integration.api.http.CpfRestClient;
import java.util.Map;
/** 외부 Side Effect 호출 결과를 transport 4상태로 보존합니다. */
public final class TransferInstitutionClient {
 private final CpfRestClient rest; public TransferInstitutionClient(CpfRestClient rest){this.rest=rest;}
 public CpfResult<String> transfer(TransferCommand command){return rest.exchangeResult("settlement-agency","POST",b->b.path("/requests").build(),command,Map.of("X-Idempotency-Key",command.idempotencyKey()),String.class);}
 public CpfResult<String> probe(String idempotencyKey){return rest.exchangeResult("settlement-agency","GET",b->b.path("/requests/{id}/status").build(idempotencyKey),null,Map.of(),String.class);}
}
