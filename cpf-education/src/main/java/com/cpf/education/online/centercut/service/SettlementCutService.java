package com.cpf.education.online.centercut.service;
import com.cpf.batch.api.*;import com.cpf.core.api.context.CpfContexts;import com.cpf.education.online.centercut.dto.*;import com.cpf.foundation.annotation.CpfService;import java.util.Map;
/** Center-Cut 교육 예제의 Service 역할과 CPF 표준 사용 경계를 보여줍니다. */
@CpfService public class SettlementCutService { private final CpfCenterCutOperations centerCut; public SettlementCutService(CpfCenterCutOperations c){this.centerCut=c;}
 /** Center-Cut 예제에서 launch 요청을 표준 호출 흐름으로 처리합니다. */
 public CenterCutView launch(SettlementCutCommand c)throws Exception{Map<String,Object> accepted=centerCut.launch(new CenterCutExecutionRequest("EDU-BATCH-05",c.idempotencyKey(),c.parameters(),"1",c.tpsLimit(),c.concurrencyLimit(),c.requestedBy(),"교육 Center-Cut 실행",CpfContexts.transactionId(),CpfContexts.currentSegmentId()));String id=String.valueOf(accepted.getOrDefault("executionId",CpfContexts.currentSegmentId()));return new CenterCutView(id,CpfContexts.transactionId(),accepted,centerCut.status(id));}}
