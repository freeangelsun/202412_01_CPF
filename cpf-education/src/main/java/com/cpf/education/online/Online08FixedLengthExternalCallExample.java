package com.cpf.education.online;

import com.cpf.foundation.execution.api.CpfOnlineTransaction; import com.cpf.integration.api.annotation.CpfTimeLimiter; import com.cpf.integration.api.http.CpfRestClient; import com.cpf.integration.fixedlength.api.*; import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation; import java.util.*; import org.springframework.web.bind.annotation.*;
@CpfRestController @RequestMapping("/edu/online/08-fixed-length")
/** 온라인-08 고정길이 전문 외부 거래 호출: 최신 CPF Public API Golden Path를 실제 업무 흐름으로 보여주는 실행 예제입니다. */
public class Online08FixedLengthExternalCallExample {
 private final CpfFixedLengthOperations fixed; private final CpfRestClient rest; public Online08FixedLengthExternalCallExample(CpfFixedLengthOperations fixed,CpfRestClient rest){this.fixed=fixed;this.rest=rest;}
 @PostMapping @CpfTimeLimiter(name="eduFixedLengthExternal") @Operation(operationId="EDU-ONLINE-08",summary="고정길이 전문 외부 거래 호출")
 @CpfOnlineTransaction(operationId="EDU-ONLINE-08",name="고정길이 전문 외부 거래 호출",description="CPF Fixed Length layout으로 byte-length를 검증해 전문을 생성·송신·수신·Parsing한다.")
 public CpfFixedLengthParseResult call(@RequestBody Map<String,Object> fields){var write=fixed.write(fields,"EDU-CREDIT-REQ","1");String response=rest.post("legacy-credit","/telegram",write.message(),String.class);return fixed.parse(response,"EDU-CREDIT-RES","1");}
}
