package com.cpf.reference.telegram;

import com.cpf.common.message.fixedlength.FixedLengthFieldSpec;
import com.cpf.common.message.fixedlength.FixedLengthLayoutSpec;

import java.util.List;

/**
 * REF 업무가 CMN 고정길이 layout을 사용하는 샘플입니다.
 */
public class ReferenceFixedLengthBusinessUseEducationSample {

    public FixedLengthLayoutSpec layout() {
        return FixedLengthLayoutSpec.utf8(12, List.of(
                FixedLengthFieldSpec.of("bankCode", 1, 3),
                FixedLengthFieldSpec.of("userNo", 4, 9)
        ));
    }
}
