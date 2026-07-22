package com.cpf.core.common.header;

/**
 * CPF 표준 헤더 한 건에 대한 문서, 검증, 로그, DB 저장 기준입니다.
 *
 * @param name                HTTP 헤더명
 * @param category            운영 분류
 * @param description         운영자와 개발자가 보는 한글 설명
 * @param producer            값을 생성하거나 보강하는 주체
 * @param validationPoint     값을 검증하거나 확정하는 위치
 * @param propagation         하위 서비스 호출 시 자동 전파 여부
 * @param responseHeader      응답 헤더로 돌려줄 수 있는 값인지 여부
 * @param dbColumn            거래 로그 대표 컬럼명. 상세 영역만 저장하면 null을 사용합니다.
 * @param recommendedDbLength DB 저장 시 권장 길이
 * @param loggable            원문 로그 저장 가능 여부
 * @param masked              로그/ADM 표시 시 마스킹 필요 여부
 * @param admSection          ADM 로그 상세 화면에서 표시할 영역명
 */
public record CpfHeaderSpec(
        String name,
        CpfHeaderCategory category,
        String description,
        String producer,
        String validationPoint,
        boolean propagation,
        boolean responseHeader,
        String dbColumn,
        int recommendedDbLength,
        boolean loggable,
        boolean masked,
        String admSection) {
}
