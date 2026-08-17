package com.cpf.education.online;

import com.cpf.foundation.execution.api.CpfOnlineTransaction; import com.cpf.integration.api.annotation.CpfRetry; import com.cpf.integration.api.annotation.CpfTimeLimiter; import com.cpf.integration.api.http.CpfRestClient; import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation; import java.util.Map; import org.springframework.web.bind.annotation.*;
@CpfRestController @RequestMapping("/edu/online/07-external-rest")
/** 온라인-07 외부 REST API 거래 호출: 최신 CPF Public API Golden Path를 실제 업무 흐름으로 보여주는 실행 예제입니다. */
public class Online07ExternalRestCallExample {
 private final CpfRestClient rest; public Online07ExternalRestCallExample(CpfRestClient rest){this.rest=rest;}
 @GetMapping @CpfRetry(name="eduExternalRest") @CpfTimeLimiter(name="eduExternalRest") @Operation(operationId="EDU-ONLINE-07",summary="외부 REST API 거래 호출")
 @CpfOnlineTransaction(operationId="EDU-ONLINE-07",name="외부 REST API 거래 호출",description="CPF RestClient가 timeout/retry/trace/masking 정책을 적용하며 내부 CPF Header를 외부에 기본 유출하지 않는다.")
 public ExternalReply get(@RequestParam String accountId){return rest.get("credit-agency",b->b.path("/v1/accounts/{id}").build(accountId),ExternalReply.class);}
 public record ExternalReply(String status,Map<String,Object> detail){}
}
