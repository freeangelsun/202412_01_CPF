package cpf.pfw.common.exception;

import java.util.Map;

/**
 * PFW ?꾨젅?꾩썙??肄붿뼱?먯꽌 諛쒖깮?섎뒗 ?쒖? ?덉쇅?낅땲??
 *
 * <p>?낅Т 洹쒖튃 ?꾨컲???꾨땲??嫄곕옒 ?ㅻ뜑, 嫄곕옒 硫뷀??곗씠?? ?꾨젅?꾩썙???ㅼ젙泥섎읆
 * ?꾨젅?꾩썙???먯껜 湲곗????꾨컲?덉쓣 ???ъ슜?⑸땲??</p>
 */
public class FpsFrameworkException extends FpsException {

    public FpsFrameworkException(FpsFrameworkErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public FpsFrameworkException(
            FpsFrameworkErrorCode errorCode,
            String detail,
            Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }
}

