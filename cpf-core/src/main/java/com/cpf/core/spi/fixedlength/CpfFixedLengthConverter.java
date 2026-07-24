package com.cpf.core.spi.fixedlength;

import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;

/**
 * 고정길이 필드의 논리값과 wire 문자열을 변환하는 확장 SPI입니다.
 *
 * <p>구현체는 상태를 요청 간 공유하지 않는 thread-safe 객체여야 합니다.</p>
 */
public interface CpfFixedLengthConverter {
    String id();

    boolean supports(CpfFixedLengthFieldSpec field);

    String format(CpfFixedLengthFieldSpec field, Object value);

    Object parse(CpfFixedLengthFieldSpec field, String value);
}
