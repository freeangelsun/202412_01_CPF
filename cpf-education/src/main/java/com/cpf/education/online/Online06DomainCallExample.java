package com.cpf.education.online;

import com.cpf.core.api.base.*; import com.cpf.core.api.result.CpfResult;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.integration.api.domaincall.CpfDomainClientRouter;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
@CpfRestController @RequestMapping("/edu/online/06-domain")
/** 온라인-06 CPF Domain 간 거래 호출: 최신 CPF Public API Golden Path를 실제 업무 흐름으로 보여주는 실행 예제입니다. */
public class Online06DomainCallExample {
 private final CpfDomainClientRouter domains; public Online06DomainCallExample(CpfDomainClientRouter domains){this.domains=domains;}
 @PostMapping @Operation(operationId="EDU-ONLINE-06",summary="CPF Domain 간 호출")
 @CpfOnlineTransaction(operationId="EDU-ONLINE-06",name="CPF Domain 간 거래 호출",description="Typed Domain Client가 동일 JVM/원격 WAS를 선택하고 거래 Context를 자동 전파한다.")
 public CpfResult<MemberReply> call(@RequestBody MemberRequest request){return domains.invoke("EXS","EXS-MEMBER-LOOKUP",request,MemberReply.class);}
 public record MemberRequest(String memberId) implements CpfRequest{} public record MemberReply(String status) implements CpfResponse{}
}
