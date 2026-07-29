package com.cpf.member.sampleitem.dto;
import java.util.List;
/** Typed 검색 결과와 정규화된 조회 조건입니다. */
public record MemberSearchResult(List<MemberSampleItem> items,MemberSearchRequest criteria,long totalCount){public MemberSearchResult{items=items==null?List.of():List.copyOf(items);}}