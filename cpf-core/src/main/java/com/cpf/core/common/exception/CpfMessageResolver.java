package com.cpf.core.common.exception;

import java.util.Locale;

/** 오류 정의와 Locale을 외부·내부 메시지 쌍으로 해석하는 계약입니다. */
public interface CpfMessageResolver {

    /** 메시지 저장소를 조회하고 누락 시 오류 정의의 기본 메시지를 반환합니다. */
    CpfResolvedMessage resolve(CpfErrorDefinition errorCode, Locale locale);
}

