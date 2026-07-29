package com.cpf.core.api.error;

import java.util.Locale;

/** 오류 정의와 Locale을 외부·내부 메시지 쌍으로 해석하는 공개 SPI입니다. */
public interface CpfMessageResolver {
    CpfResolvedMessage resolve(CpfErrorDefinition errorCode, Locale locale);
}
