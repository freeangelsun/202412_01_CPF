package com.cpf.member.sampleitem.service;

import com.cpf.member.common.base.MemberBaseService;
import com.cpf.member.sampleitem.dto.*;
import com.cpf.member.sampleitem.port.*;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.page.CpfSlice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import java.util.Optional;

/** Generated Domain Typed Sample Item 업무 서비스입니다. */
@Service
public class MemberService extends MemberBaseService {
    private final MemberQueryPort queryPort; private final MemberCommandPort commandPort;
    public MemberService(MemberQueryPort queryPort,MemberCommandPort commandPort){this.queryPort=Objects.requireNonNull(queryPort);this.commandPort=Objects.requireNonNull(commandPort);}
    @Transactional(readOnly=true) public MemberSearchResult search(MemberSearchRequest r){return queryPort.search(r.normalized());}
    @Transactional public MemberSampleItem create(MemberSampleCommand c){var x=context();return commandPort.create(c,x.tx(),x.idem(),x.seq(),x.actor());}
    @Transactional(readOnly=true) public Optional<MemberSampleItem> findBySampleKey(String key){if(key==null||key.isBlank())throw new CpfValidationException("sampleKey는 필수입니다.");return queryPort.findBySampleKey(key.trim());}
    @Transactional public MemberSampleItem update(long id,MemberSampleCommand c){if(id<1)throw new CpfValidationException("sampleItemId는 1 이상이어야 합니다.");var x=context();return commandPort.update(id,c,x.tx(),x.idem(),x.seq(),x.actor());}
    @Transactional public MemberDeleteResult delete(long id,MemberDeleteCommand c){if(id<1||c==null)throw new CpfValidationException("삭제 입력이 올바르지 않습니다.");var x=context();return commandPort.delete(id,c.expectedVersion(),x.tx(),x.idem(),x.seq(),x.actor());}
    @Transactional(readOnly=true) public CpfSlice<MemberSampleItem> cursor(Long afterId,int size){return queryPort.cursor(afterId,size);}
    public boolean verifyRollback(MemberSampleCommand c){var x=context();return commandPort.verifyRollback(c,x.tx(),x.idem(),x.seq(),x.actor());}
    private MutationContext context(){String idem=CpfTransactionContext.idempotencyKey();if(idem==null||idem.isBlank())throw new CpfValidationException("변경 거래에는 idempotencyKey가 필수입니다.");String actor=first(CpfTransactionContext.operatorId(),CpfTransactionContext.userId(),"MBR");return new MutationContext(CpfTransactionContext.transactionId(),idem.trim(),CpfTransactionContext.nextSequence(),actor);}
    private String first(String... values){for(String v:values)if(v!=null&&!v.isBlank())return v.trim();return "MBR";}
    private record MutationContext(String tx,String idem,long seq,String actor){}
}