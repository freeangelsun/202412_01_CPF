package com.cpf.core.common.exception;

import java.util.Locale;

/**
 * CMN 메시지 카탈로그를 사용할 수 없을 때 오류 코드의 기본 메시지를 반환합니다.
 */
public class DefaultCpfMessageResolver implements CpfMessageResolver {

    @Override
    public CpfResolvedMessage resolve(CpfErrorDefinition errorCode, Locale locale) {
        return new CpfResolvedMessage(
                errorCode.getDefaultExternalMessage(),
                errorCode.getDefaultInternalMessage());
    }
}
