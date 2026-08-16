package com.cpf.foundation.message;

import com.cpf.core.api.error.CpfErrorDefinition;
import java.util.Locale;

/** 별도 카탈로그가 없을 때 Core 오류 정의의 기본 메시지를 안전하게 사용하는 기본 구현입니다. */
public final class DefaultCpfMessageResolver implements CpfMessageResolver {
    @Override public CpfResolvedMessage resolve(CpfErrorDefinition code, Locale locale) {
        if (code == null) return new CpfResolvedMessage("처리 중 오류가 발생했습니다.", "CPF 처리 중 오류가 발생했습니다.");
        String external = code.exposure() == CpfErrorDefinition.Exposure.GENERIC_MESSAGE_ONLY
                ? "처리 중 오류가 발생했습니다." : code.defaultExternalMessage();
        return new CpfResolvedMessage(external, code.defaultInternalMessage());
    }
}
