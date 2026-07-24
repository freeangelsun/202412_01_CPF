package com.cpf.core.api.fixedlength;

/**
 * 고정길이 전문을 layout에 따라 해석하는 Core 공개 API입니다.
 *
 * <p>구현체는 여러 요청과 thread가 동시에 재사용할 수 있어야 합니다.</p>
 */
public interface CpfFixedLengthParser {
    CpfFixedLengthParseResult parse(String message, CpfFixedLengthLayout layout);

    CpfFixedLengthParseResult parse(byte[] message, CpfFixedLengthLayout layout);
}
