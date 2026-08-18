package com.cpf.education.online.recovery.service;
import com.cpf.core.api.result.*; import com.cpf.education.online.recovery.client.TransferInstitutionClient; import com.cpf.education.online.recovery.dto.TransferCommand; import com.cpf.foundation.annotation.CpfService;
@CpfService
/** TransferService는 UNKNOWN 결과를 Probe/Reconcile/Recovery 경로로 수렴시키는 복구 Golden Path입니다. */
public class TransferService { private final TransferInstitutionClient client; public TransferService(TransferInstitutionClient client){this.client=client;}
 /** execute 동작은 UNKNOWN 결과를 Probe/Reconcile/Recovery 경로로 수렴시키는 복구 Golden Path에서 필요한 공개 동작을 수행합니다. */
 public CpfResult<String> execute(TransferCommand command){ CpfResult<String> result=client.transfer(command); if(!result.isUnknown()) return result; return CpfResult.unknown(result.errorCode(), result.errorMessage(), new CpfRecoveryInfo(command.idempotencyKey(),"PROBE_OR_RECONCILE")); }
}
