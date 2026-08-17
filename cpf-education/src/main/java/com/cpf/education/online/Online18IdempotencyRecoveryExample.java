package com.cpf.education.online;

import com.cpf.core.api.result.*; import com.cpf.foundation.execution.api.CpfOnlineTransaction; import com.cpf.integration.api.http.CpfRestClient; import com.cpf.reliability.api.CpfIdempotent; import com.cpf.web.api.CpfRestController; import io.swagger.v3.oas.annotations.Operation; import org.springframework.web.bind.annotation.*;
@CpfRestController @RequestMapping("/edu/online/18-recovery")
/** 온라인-18 Idempotency·UNKNOWN·Recovery 거래: 최신 CPF Public API Golden Path를 실제 업무 흐름으로 보여주는 실행 예제입니다. */
public class Online18IdempotencyRecoveryExample {
 private final CpfRestClient rest; public Online18IdempotencyRecoveryExample(CpfRestClient rest){this.rest=rest;}
 @PostMapping @CpfIdempotent(operation="EDU-ONLINE-18") @Operation(operationId="EDU-ONLINE-18",summary="Idempotency·UNKNOWN·Recovery 거래")
 @CpfOnlineTransaction(operationId="EDU-ONLINE-18",name="Idempotency·UNKNOWN·Recovery 거래",description="외부 Side Effect의 결과불명 상태를 blind retry하지 않고 CPF Recovery/Reconcile 경로로 연결한다.")
 public CpfResult<String> execute(@RequestBody Command c){try{return CpfResult.success(rest.post("settlement-agency","/requests",c,String.class));}catch(RuntimeException ex){return CpfResult.unknown("UNKNOWN_RESULT","결과 확정 전 재호출하지 않습니다.",new CpfRecoveryInfo(c.idempotencyKey(),"RECONCILE"));}} public record Command(String idempotencyKey,String payload){}
}
