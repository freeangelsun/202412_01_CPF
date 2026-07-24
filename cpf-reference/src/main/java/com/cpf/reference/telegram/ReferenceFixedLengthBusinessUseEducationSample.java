package com.cpf.reference.telegram;

import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayout;

import java.util.List;

/**
 * REF 업무가 CPF Core 고정길이 layout을 사용하는 샘플입니다.
 */
public class ReferenceFixedLengthBusinessUseEducationSample {

    public CpfFixedLengthLayout layout() {
        return CpfFixedLengthLayout.utf8(12, List.of(
                CpfFixedLengthFieldSpec.of("bankCode", 1, 3),
                CpfFixedLengthFieldSpec.of("userNo", 4, 9)
        ));
    }
}
