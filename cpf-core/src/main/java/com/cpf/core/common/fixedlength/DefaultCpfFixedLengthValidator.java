package com.cpf.core.common.fixedlength;

import com.cpf.core.api.fixedlength.CpfFixedLengthError;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;
import com.cpf.core.spi.fixedlength.CpfFixedLengthValidator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 필수값 정책을 검사하는 CPF 기본 validator입니다.
 */
@Component
public final class DefaultCpfFixedLengthValidator implements CpfFixedLengthValidator {
    @Override
    public boolean supports(CpfFixedLengthFieldSpec field) {
        return true;
    }

    @Override
    public List<CpfFixedLengthError> validate(
            CpfFixedLengthFieldSpec field,
            String fieldPath,
            String value,
            int byteOffset) {
        if (field.required() && (value == null || value.isBlank())) {
            return List.of(new CpfFixedLengthError(
                    fieldPath,
                    "CPF_FIXED_FIELD_REQUIRED",
                    "필수 고정길이 필드 값이 비어 있습니다.",
                    byteOffset,
                    byteOffset + 1,
                    ""));
        }
        return List.of();
    }
}
