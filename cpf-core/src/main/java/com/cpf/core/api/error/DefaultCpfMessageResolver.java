package com.cpf.core.api.error;

import java.util.Locale;

/** 메시지 카탈로그가 없을 때 오류 정의의 기본 메시지를 사용하는 공개 기본 구현입니다. */
public class DefaultCpfMessageResolver implements CpfMessageResolver {
    @Override
    public CpfResolvedMessage resolve(CpfErrorDefinition errorCode, Locale locale) {
        return new CpfResolvedMessage(
                errorCode.getDefaultExternalMessage(),
                errorCode.getDefaultInternalMessage());
    }
}
