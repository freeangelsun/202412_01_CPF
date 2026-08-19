package com.cpf.education.online.internalservice.service;
import com.cpf.core.api.context.CpfContexts;import com.cpf.data.persistence.api.annotation.CpfTransactional;import com.cpf.foundation.annotation.CpfService;import org.springframework.transaction.annotation.Propagation;
/** 동일 Application 내부 호출 교육 예제의 Service 역할과 CPF 표준 사용 경계를 보여줍니다. */
@CpfService public class EntryService {private final ChildService child;public EntryService(ChildService c){child=c;}@CpfTransactional(propagation=Propagation.REQUIRED) public String process(String value){return child.process(value,CpfContexts.transactionId());}}
