package cpf.cmn.edu.fixedlength;

import cpf.cmn.message.fixedlength.FixedLengthFieldSpec;
import cpf.cmn.message.fixedlength.FixedLengthLayoutSpec;

import java.util.List;

/**
 * CMN 고정길이 layout 정의 교육 샘플입니다.
 */
public class CmnFixedLengthLayoutEducationSample {

    public FixedLengthLayoutSpec memberLayout() {
        return FixedLengthLayoutSpec.utf8(14, List.of(
                FixedLengthFieldSpec.of("channel", 1, 3),
                FixedLengthFieldSpec.of("memberNo", 4, 10),
                FixedLengthFieldSpec.of("status", 14, 1)
        ));
    }
}
