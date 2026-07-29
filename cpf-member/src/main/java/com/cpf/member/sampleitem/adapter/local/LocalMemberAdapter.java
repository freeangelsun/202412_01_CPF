package com.cpf.member.sampleitem.adapter.local;

import com.cpf.member.sampleitem.dto.*;
import com.cpf.core.api.page.CpfSlice;
import com.cpf.member.sampleitem.port.*;
import com.cpf.member.sampleitem.repository.MemberRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.Optional;

/** 같은 주제영역 DB를 사용하는 기본 Local Adapter입니다. */
@Component
@ConditionalOnProperty(name = "cpf.member.sample-item.mode", havingValue = "local", matchIfMissing = true)
public class LocalMemberAdapter implements MemberQueryPort, MemberCommandPort {
    private final MemberRepository repository;
    public LocalMemberAdapter(MemberRepository repository) { this.repository = Objects.requireNonNull(repository); }
    public MemberSearchResult search(MemberSearchRequest request) { return repository.search(request); }
    public Optional<MemberSampleItem> findBySampleKey(String sampleKey) { return repository.findBySampleKey(sampleKey); }
    public CpfSlice<MemberSampleItem> cursor(Long afterId, int size) { return repository.cursor(afterId, size); }
    public MemberSampleItem create(MemberSampleCommand command,String tx,String key,long seq,String actor){return repository.create(command,tx,key,seq,actor);}
    public MemberSampleItem update(long id,MemberSampleCommand command,String tx,String key,long seq,String actor){return repository.update(id,command,tx,key,seq,actor);}
    public MemberDeleteResult delete(long id,long version,String tx,String key,long seq,String actor){return repository.delete(id,version,tx,key,seq,actor);}
    public boolean verifyRollback(MemberSampleCommand command,String tx,String key,long seq,String actor){return repository.verifyRollback(command,tx,key,seq,actor);}
}