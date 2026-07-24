package com.cpf.core.spi.fixedlength;

import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;

/**
 * 민감 필드의 진단·결과 노출 값을 안전하게 바꾸는 확장 SPI입니다.
 */
public interface CpfFixedLengthMasker {
    String mask(CpfFixedLengthFieldSpec field, String value);
}
