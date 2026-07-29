package com.cpf.member.sampleitem.port;

import com.cpf.member.common.contract.MemberRepositoryPort;
import com.cpf.member.sampleitem.dto.*;
import com.cpf.core.api.page.CpfSlice;
import java.util.Optional;

/** 조회 책임만 소유하는 Generated Domain Query Port입니다. */
public interface MemberQueryPort extends MemberRepositoryPort<MemberSampleItem, String> {
    MemberSearchResult search(MemberSearchRequest request);
    Optional<MemberSampleItem> findBySampleKey(String sampleKey);
    CpfSlice<MemberSampleItem> cursor(Long afterId, int size);
}