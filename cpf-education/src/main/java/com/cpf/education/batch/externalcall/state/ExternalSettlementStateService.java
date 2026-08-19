package com.cpf.education.batch.externalcall.state;

import com.cpf.data.persistence.api.CpfCrudRepository;
import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.education.batch.externalcall.model.ExternalSettlementState;
import com.cpf.foundation.annotation.CpfService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.Propagation;

@CpfService
/** 외부 연계 교육 예제의 State 역할과 CPF 표준 사용 경계를 보여줍니다. */
public class ExternalSettlementStateService {
    private final ObjectProvider<CpfCrudRepository<ExternalSettlementState,String>> repositories;
    /** 외부 연계 예제의 State 의존성을 주입해 표준 실행 경계를 구성합니다. */
    public ExternalSettlementStateService(ObjectProvider<CpfCrudRepository<ExternalSettlementState,String>> repositories){this.repositories=repositories;}
    @CpfTransactional(propagation=Propagation.REQUIRES_NEW)
    /** 외부 연계 예제에서 mark 요청을 표준 호출 흐름으로 처리합니다. */
    public void mark(String id,String status,String reference){
        CpfCrudRepository<ExternalSettlementState,String> repository=repositories.getIfAvailable();
        if(repository==null)throw new IllegalStateException("CPF batch state repository is not configured");
        repository.save(new ExternalSettlementState(id,status,reference));
    }
}
