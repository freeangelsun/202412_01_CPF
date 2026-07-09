package cpf.xyz.edu.telegram;

import cpf.cmn.message.fixedlength.FixedLengthFieldSpec;
import cpf.cmn.message.fixedlength.FixedLengthLayoutSpec;

import java.util.List;

/**
 * XYZ 업무가 CMN 고정길이 layout을 사용하는 샘플입니다.
 */
public class XyzFixedLengthBusinessUseEducationSample {

    public FixedLengthLayoutSpec layout() {
        return FixedLengthLayoutSpec.utf8(12, List.of(
                FixedLengthFieldSpec.of("bankCode", 1, 3),
                FixedLengthFieldSpec.of("userNo", 4, 9)
        ));
    }
}
