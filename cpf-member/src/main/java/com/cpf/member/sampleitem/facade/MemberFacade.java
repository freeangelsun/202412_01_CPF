package com.cpf.member.sampleitem.facade;

import com.cpf.member.common.contract.MemberApplicationFacade;
import com.cpf.member.sampleitem.dto.*;
import com.cpf.core.api.page.CpfSlice;
import com.cpf.member.sampleitem.service.MemberService;
import org.springframework.stereotype.Component;
import java.util.Objects;
import java.util.Optional;

/** Controller와 업무 서비스를 분리하는 Generated Domain 진입 Facade입니다. */
@Component
public class MemberFacade implements MemberApplicationFacade {
    private final MemberService service;
    public MemberFacade(MemberService service) { this.service = Objects.requireNonNull(service); }
    public MemberSearchResult search(MemberSearchRequest request) { return service.search(request); }
    public MemberSampleItem create(MemberSampleCommand command) { return service.create(command); }
    public Optional<MemberSampleItem> findBySampleKey(String sampleKey) { return service.findBySampleKey(sampleKey); }
    public MemberSampleItem update(long sampleItemId, MemberSampleCommand command) { return service.update(sampleItemId, command); }
    public MemberDeleteResult delete(long sampleItemId, MemberDeleteCommand command) { return service.delete(sampleItemId, command); }
    public CpfSlice<MemberSampleItem> cursor(Long afterId, int size) { return service.cursor(afterId, size); }
    public boolean verifyRollback(MemberSampleCommand command) { return service.verifyRollback(command); }
}