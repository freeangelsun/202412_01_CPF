package com.cpf.education.online;

import com.cpf.common.calendar.api.CpfCalendarService;
import com.cpf.common.code.api.CpfCodeService;
import com.cpf.common.message.api.CpfMessageSource;
import com.cpf.common.parameter.api.CpfParameterService;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate; import java.util.*;
import org.springframework.web.bind.annotation.*;
@CpfRestController @RequestMapping("/edu/online/03-common")
/** 온라인-03 공통 코드·메시지·파라미터·영업일 사용 거래: 최신 CPF Public API Golden Path를 실제 업무 흐름으로 보여주는 실행 예제입니다. */
public class Online03CommonCatalogExample {
 private final CpfCodeService codes; private final CpfMessageSource messages; private final CpfParameterService parameters; private final CpfCalendarService calendars;
 /** 공통 코드·메시지·파라미터·영업일 Consumer를 예제에 주입합니다. */
 public Online03CommonCatalogExample(CpfCodeService codes,CpfMessageSource messages,CpfParameterService parameters,CpfCalendarService calendars){this.codes=codes;this.messages=messages;this.parameters=parameters;this.calendars=calendars;}
 @GetMapping @Operation(operationId="EDU-ONLINE-03",summary="코드·메시지·파라미터·영업일")
 @CpfOnlineTransaction(operationId="EDU-ONLINE-03",name="공통정보 사용 거래",description="CPF Common Code/Message/Parameter/Calendar API를 함께 사용한다.")
 public Map<String,Object> resolve(@RequestParam String status,@RequestParam String date){var d=LocalDate.parse(date);return Map.of("status",codes.required("MEMBER_STATUS",status),"limit",parameters.requiredValue("member.daily-limit",Long.class),"message",messages.getMessage("member.available",Locale.KOREAN),"businessDay",calendars.isBusinessDay("KR",d));}
}
