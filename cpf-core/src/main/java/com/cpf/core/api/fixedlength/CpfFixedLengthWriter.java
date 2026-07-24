package com.cpf.core.api.fixedlength;

import java.util.Map;

/**
 * 값을 layout의 정확한 byte 길이 전문으로 생성하는 Core 공개 API입니다.
 *
 * <p>구현체는 여러 요청과 thread가 동시에 재사용할 수 있어야 합니다.</p>
 */
public interface CpfFixedLengthWriter {
    CpfFixedLengthWriteResult write(Map<String, ?> values, CpfFixedLengthLayout layout);
}
