package com.cpf.education.online.recovery.controller;
import com.cpf.core.api.result.CpfResult; import com.cpf.education.online.recovery.dto.TransferCommand; import com.cpf.education.online.recovery.service.TransferService; import com.cpf.foundation.execution.api.CpfOnlineTransaction; import com.cpf.reliability.api.CpfIdempotent; import com.cpf.web.api.CpfController; import io.swagger.v3.oas.annotations.Operation; import org.springframework.web.bind.annotation.*;
@CpfController @RequestMapping("/edu/online/transfers")
/** TransferController는 UNKNOWN 결과를 Probe/Reconcile/Recovery 경로로 수렴시키는 복구 Golden Path입니다. */
public class TransferController { private final TransferService service; public TransferController(TransferService service){this.service=service;}
 @PostMapping @CpfIdempotent(operation="EDU_TRANSFER_RECOVERY") @Operation(operationId="EDU_TRANSFER_RECOVERY",summary="UNKNOWN·Recovery") @CpfOnlineTransaction(operationId="EDU_TRANSFER_RECOVERY",name="송금 결과 대사",description="UNKNOWN은 blind retry하지 않고 Probe/Reconcile로 연결한다.")
 public CpfResult<String> execute(@RequestBody TransferCommand command){return service.execute(command);} }
