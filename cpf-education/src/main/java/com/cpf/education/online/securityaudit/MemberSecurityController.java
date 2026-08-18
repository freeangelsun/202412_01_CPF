package com.cpf.education.online.securityaudit;

import com.cpf.core.api.context.CpfContexts; import com.cpf.foundation.execution.api.CpfOnlineTransaction; import com.cpf.security.api.CpfMasking; import com.cpf.security.api.annotation.CpfPreAuthorize; import com.cpf.security.api.audit.*; import com.cpf.web.api.CpfRestController; import io.swagger.v3.oas.annotations.Operation; import java.time.Instant; import java.util.Map; import org.springframework.web.bind.annotation.*;
@CpfRestController @RequestMapping("/edu/online/member-security")
/** 온라인-17 Security·Permission·Audit 거래: 최신 CPF Public API Golden Path를 실제 업무 흐름으로 보여주는 실행 예제입니다. */
public class MemberSecurityController {
 private final CpfAuthorizationAuditSink audit; public MemberSecurityController(CpfAuthorizationAuditSink audit){this.audit=audit;}
 @PostMapping @CpfPreAuthorize("hasAuthority('MEMBER_READ')") @Operation(operationId="EDU_SECURE_MEMBER_LOOKUP",summary="Security·Permission·Audit 거래")
 @CpfOnlineTransaction(operationId="EDU_SECURE_MEMBER_LOOKUP",name="Security·Permission·Audit 거래",description="Spring Method Security 의미를 따르는 CPF 권한 Annotation과 CPF Audit/Masking을 사용한다.")
 public Map<String,String> read(@RequestBody Command c){audit.record(new CpfAuthorizationAuditEvent("AUTHORIZATION","MEMBER_READ",CpfContexts.transactionId(),CpfContexts.currentExecutionId(),CpfContexts.userId(),CpfContexts.operatorId(),true,"교육 권한 허용",Instant.now()));return Map.of("memberId",c.memberId(),"mobile",CpfMasking.mobile(c.mobile()));} public record Command(String memberId,String mobile){}
}
