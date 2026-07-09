package cpf.cmn.edu.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 공통 변환 helper 샘플입니다.
 */
public class CmnConverterEducationSample {

    public BigDecimal amount(String rawAmount) {
        return new BigDecimal(rawAmount).setScale(2, RoundingMode.UNNECESSARY);
    }
}
