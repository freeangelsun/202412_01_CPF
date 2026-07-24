package com.cpf.core.common.fixedlength;

import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;
import com.cpf.core.spi.fixedlength.CpfFixedLengthMasker;
import org.springframework.stereotype.Component;

/**
 * 앞뒤 한 문자만 남기는 CPF 기본 민감 필드 마스킹 정책입니다.
 */
@Component
public final class DefaultCpfFixedLengthMasker implements CpfFixedLengthMasker {
    @Override
    public String mask(CpfFixedLengthFieldSpec field, String value) {
        if (value == null || !field.sensitive()) {
            return value;
        }
        if (value.isEmpty()) {
            return "";
        }
        int codePointCount = value.codePointCount(0, value.length());
        if (codePointCount <= 2) {
            return "*".repeat(codePointCount);
        }
        int firstEnd = value.offsetByCodePoints(0, 1);
        int lastStart = value.offsetByCodePoints(0, codePointCount - 1);
        return value.substring(0, firstEnd)
                + "*".repeat(codePointCount - 2)
                + value.substring(lastStart);
    }
}
