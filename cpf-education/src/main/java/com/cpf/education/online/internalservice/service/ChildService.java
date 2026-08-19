package com.cpf.education.online.internalservice.service;
import com.cpf.core.api.context.CpfContexts;import com.cpf.data.persistence.api.annotation.CpfTransactional;import com.cpf.foundation.annotation.CpfService;import org.springframework.transaction.annotation.Propagation;
/** 동일 Application 내부 호출 교육 예제의 Service 역할과 CPF 표준 사용 경계를 보여줍니다. */
@CpfService public class ChildService {@CpfTransactional(propagation=Propagation.REQUIRED) public String process(String value,String expected){if(!expected.equals(CpfContexts.transactionId()))throw new IllegalStateException("CPF context continuity failed");if("FAIL".equalsIgnoreCase(value))throw new IllegalArgumentException("업무 처리 실패 예시");return "processed:"+value;}}
