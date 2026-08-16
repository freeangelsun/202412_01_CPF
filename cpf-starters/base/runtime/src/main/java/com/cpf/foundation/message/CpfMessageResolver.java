package com.cpf.foundation.message;

import com.cpf.core.api.error.CpfErrorDefinition;
import java.util.Locale;

/** 오류 정의를 Locale별 안전/운영 메시지로 해석하는 기술 공통 계약입니다. */
public interface CpfMessageResolver {
    CpfResolvedMessage resolve(CpfErrorDefinition errorCode, Locale locale);
}
