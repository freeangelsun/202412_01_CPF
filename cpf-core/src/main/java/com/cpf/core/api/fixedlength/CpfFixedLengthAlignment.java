package com.cpf.core.api.fixedlength;

/**
 * 고정길이 필드 값과 padding 문자의 정렬 방식입니다.
 */
public enum CpfFixedLengthAlignment {
    /**
     * 자료형에 맞는 기본 정렬을 사용합니다. 문자열·날짜·시간은 왼쪽,
     * 숫자·금액·불리언은 오른쪽 정렬합니다.
     */
    AUTO,
    LEFT,
    RIGHT
}
