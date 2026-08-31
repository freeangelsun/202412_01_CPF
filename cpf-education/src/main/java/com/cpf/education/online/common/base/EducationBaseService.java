package com.cpf.education.online.common.base;

import com.cpf.foundation.api.CpfBaseService;

/**
 * CPF Education 업무 Service의 주제영역 공통 확장점입니다.
 *
 * <p>CPF는 {@code Framework Base -> Domain Base(abstract) -> Business(concrete)} 3단 구조를
 * 기동 시점에 fail-closed로 검증합니다({@code CpfThreeTierStructurePolicy}). Education 모듈은
 * 그 Domain Base가 없어 {@code @CpfService} Bean이 등록되는 순간 기동이 실패했습니다.</p>
 *
 * <p>EDU는 최종 package root가 {@code online}/{@code batch} 두 개로 고정되고 그 하위 feature
 * package 집합도 계약으로 정해져 있어(verify-cpf-current-final), 공통 Base는 새로운 root를
 * 만들지 않고 이미 허용된 {@code online.common} feature 아래에 둡니다. batch Service도 같은
 * Domain Base를 사용합니다.</p>
 */
public abstract class EducationBaseService extends CpfBaseService {
}
