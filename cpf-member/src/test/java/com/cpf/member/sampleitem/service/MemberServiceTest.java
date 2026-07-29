package com.cpf.member.sampleitem.service;

import com.cpf.member.sampleitem.dto.*;
import com.cpf.member.sampleitem.port.*;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.page.CpfSlice;
import org.junit.jupiter.api.*;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class MemberServiceTest {
    private final StubPort port=new StubPort();
    private final MemberService service=new MemberService(port,port);
    @BeforeEach void context(){CpfTransactionContext.initializeForTest("20260724123456789MBR00000010000001","MBR_IDEMPOTENCY_001","generator-test");}
    @AfterEach void clear(){CpfTransactionContext.clear();}
    @Test void queryAndCommandPortsRemainSeparatedAndTyped(){var command=new MemberSampleCommand("MBR_001","Sample","ACTIVE",0);var created=service.create(command);assertThat(created.sampleKey()).isEqualTo("MBR_001");assertThat(service.findBySampleKey(created.sampleKey())).contains(created);}
    @Test void sameIdempotencyKeyAndSameRequestReplaysResult(){var command=new MemberSampleCommand("MBR_REPLAY","Replay","ACTIVE",0);var first=service.create(command);var second=service.create(command);assertThat(second).isEqualTo(first);}
    @Test void sameIdempotencyKeyAndDifferentRequestIsRejected(){service.create(new MemberSampleCommand("MBR_A","A","ACTIVE",0));assertThatThrownBy(()->service.create(new MemberSampleCommand("MBR_B","B","ACTIVE",0))).isInstanceOf(IllegalStateException.class);}
    @Test void rollbackVerificationRestoresTheOriginalState(){var command=new MemberSampleCommand("MBR_ROLLBACK","Rollback","ACTIVE",0);assertThat(service.verifyRollback(command)).isTrue();assertThat(service.findBySampleKey(command.sampleKey())).isEmpty();}
    private final class StubPort implements MemberQueryPort,MemberCommandPort {
        private MemberSampleItem item; private String idem; private String request;
        public MemberSearchResult search(MemberSearchRequest r){return new MemberSearchResult(item==null?List.of():List.of(item),r,item==null?0:1);}
        public Optional<MemberSampleItem> findBySampleKey(String key){return Optional.ofNullable(item).filter(v->v.sampleKey().equals(key));}
        public CpfSlice<MemberSampleItem> cursor(Long after,int size){return new CpfSlice<>(item==null?List.of():List.of(item),0,size,false);}
        public MemberSampleItem create(MemberSampleCommand c,String tx,String key,long seq,String actor){String canonical=c.sampleKey()+"|"+c.itemName()+"|"+c.statusCode()+"|"+c.expectedVersion();if(Objects.equals(idem,key)){if(!Objects.equals(request,canonical))throw new IllegalStateException("idempotency conflict");return item;}if(item!=null&&item.sampleKey().equals(c.sampleKey()))throw new IllegalStateException("sampleKey duplicate");Instant now=Instant.now();item=new MemberSampleItem(1,c.sampleKey(),c.itemName(),c.statusCode(),0,key,tx,seq,now,actor,now,actor,now);idem=key;request=canonical;return item;}
        public MemberSampleItem update(long id,MemberSampleCommand c,String tx,String key,long seq,String actor){if(item==null||item.sampleItemId()!=id)throw new IllegalArgumentException("not found");if(item.versionNo()!=c.expectedVersion())throw new IllegalStateException("version conflict");Instant now=Instant.now();item=new MemberSampleItem(id,c.sampleKey(),c.itemName(),c.statusCode(),item.versionNo()+1,key,tx,seq,now,item.createdBy(),item.createdAt(),actor,now);return item;}
        public MemberDeleteResult delete(long id,long version,String tx,String key,long seq,String actor){if(item==null||item.sampleItemId()!=id||item.versionNo()!=version)throw new IllegalStateException("version conflict");item=null;idem=key;return new MemberDeleteResult(true,id,version+1);}
        public boolean verifyRollback(MemberSampleCommand c,String tx,String key,long seq,String actor){var before=item;var beforeIdem=idem;var beforeRequest=request;try{create(c,tx,key,seq,actor);}finally{item=before;idem=beforeIdem;request=beforeRequest;}return Objects.equals(before,item)&&Objects.equals(beforeIdem,idem)&&Objects.equals(beforeRequest,request);}
    }
}