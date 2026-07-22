package com.cpf.common.tlm.core;

/** 고정길이 전문 필드의 채움 문자 정렬 방식입니다. */
public enum CmnTelegramAlign {
    /** 필드 유형에 따라 문자열은 왼쪽, 숫자는 오른쪽 정렬합니다. */
    AUTO,

    /** 값 뒤에 채움 문자를 추가합니다. */
    LEFT,

    /** 값 앞에 채움 문자를 추가합니다. */
    RIGHT
}

