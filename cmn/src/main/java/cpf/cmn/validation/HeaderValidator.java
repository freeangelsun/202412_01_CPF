package cpf.cmn.validation;

import cpf.cmn.dto.HeaderDTO;
import cpf.cmn.utils.ValidationUtils;

/**
 * ?ㅻ뜑 寃利??대옒??
 * HeaderDTO???좏슚?깆쓣 寃利앺븯??濡쒖쭅???쒓났?⑸땲??
 */
public class HeaderValidator {

    /**
     * HeaderDTO ?좏슚??寃利?
     *
     * @param header 寃利앺븷 HeaderDTO 媛앹껜
     */
    public void validate(HeaderDTO header) {
        ValidationUtils.validateHeader(header);
    }
}

