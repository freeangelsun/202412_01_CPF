package cpf.mbr.common.exception;

import cpf.mbr.common.response.ResponseCode;
import lombok.Getter;

/**
 * 湲덉쑖沅?API ?덉쇅 ?뺤쓽
 * - ⑤뱺 鍮꾩쫰?덉뒪 濡쒖쭅 ?덉쇅?????대옒?ㅻ? ?곸냽諛쏆븘 ?뺤쓽
 * - ?묐떟 肄붾뱶? 硫붿떆吏瑜??④퍡 愿由?
 * 
 * @author CPF Team
 * @version 1.0.0
 */
@Getter
public class ApiException extends RuntimeException {
    
    /** ?묐떟 肄붾뱶 */
    private final ResponseCode responseCode;
    
    /** ?먮윭 硫붿떆吏 */
    private final String errorMessage;
    
    /** ?먮윭 ?곸꽭 ?뺣낫 */
    private final String details;
    
    /**
     * ?앹꽦??1: ?묐떟 肄붾뱶留??ъ슜
     * @param responseCode ?묐떟 肄붾뱶
     */
    public ApiException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
        this.errorMessage = responseCode.getMessage();
        this.details = null;
    }
    
    /**
     * ?앹꽦??2: ?묐떟 肄붾뱶? 而ㅼ뒪? 硫붿떆吏
     * @param responseCode ?묐떟 肄붾뱶
     * @param errorMessage 而ㅼ뒪? ?먮윭 硫붿떆吏
     */
    public ApiException(ResponseCode responseCode, String errorMessage) {
        super(errorMessage);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.details = null;
    }
    
    /**
     * ?앹꽦??3: ?묐떟 肄붾뱶, 硫붿떆吏, ?곸꽭 ?뺣낫
     * @param responseCode ?묐떟 肄붾뱶
     * @param errorMessage ?먮윭 硫붿떆吏
     * @param details ?곸꽭 ?뺣낫
     */
    public ApiException(ResponseCode responseCode, String errorMessage, String details) {
        super(errorMessage);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.details = details;
    }
    
    /**
     * ?앹꽦??4: ?묐떟 肄붾뱶, 硫붿떆吏, ?곸꽭 ?뺣낫, Cause
     * @param responseCode ?묐떟 肄붾뱶
     * @param errorMessage ?먮윭 硫붿떆吏
     * @param details ?곸꽭 ?뺣낫
     * @param cause ?먯씤 ?덉쇅
     */
    public ApiException(ResponseCode responseCode, String errorMessage, String details, Throwable cause) {
        super(errorMessage, cause);
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.details = details;
    }
}

