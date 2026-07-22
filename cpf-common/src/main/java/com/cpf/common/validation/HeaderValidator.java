package cpf.cmn.validation;

import cpf.cmn.dto.HeaderDTO;
import cpf.cmn.utils.ValidationUtils;

/**
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
public class HeaderValidator {

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    public void validate(HeaderDTO header) {
        ValidationUtils.validateHeader(header);
    }
}

