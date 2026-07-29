package com.cpf.member.common.contract;

import com.cpf.core.api.base.CpfRepositoryPort;

/**
 * MBR Repository Port가 공통으로 확장하는 주제영역 계약입니다.
 *
 * @param <T> model 형식
 * @param <ID> 식별자 형식
 */
public interface MemberRepositoryPort<T, ID> extends CpfRepositoryPort<T, ID> {
}