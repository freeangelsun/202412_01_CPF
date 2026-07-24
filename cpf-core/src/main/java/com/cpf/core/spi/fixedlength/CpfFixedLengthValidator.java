package com.cpf.core.spi.fixedlength;

import com.cpf.core.api.fixedlength.CpfFixedLengthError;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;

import java.util.List;

/**
 * 변환 전후 wire 값에 추가 정책을 적용하는 확장 SPI입니다.
 *
 * <p>오류 메시지에 민감 원문을 넣지 않아야 하며 구현체는 thread-safe여야 합니다.</p>
 */
public interface CpfFixedLengthValidator {
    boolean supports(CpfFixedLengthFieldSpec field);

    List<CpfFixedLengthError> validate(
            CpfFixedLengthFieldSpec field,
            String fieldPath,
            String value,
            int byteOffset);
}
