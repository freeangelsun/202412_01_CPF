package cpf.mbr.common.exception;

import cpf.mbr.common.response.BaseResponse;
import cpf.mbr.common.response.ResponseCode;
import cpf.pfw.common.exception.DefaultFpsResponseCodeResolver;
import cpf.pfw.common.exception.FpsErrorResponse;
import cpf.pfw.common.exception.FpsException;
import cpf.pfw.common.exception.FpsResolvedResponse;
import cpf.pfw.common.exception.FpsResponseCodeResolver;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 湲덉쑖沅?湲濡쒕쾶 ?덉쇅 泥섎━ ?몃뱾?? * - 紐⑤뱺 API?먯꽌 諛쒖깮?섎뒗 ?덉쇅瑜??듭씪???뺤떇?쇰줈 泥섎━
 * - ?덉쇅 濡쒓퉭 諛?媛먯떆 湲곕뒫 ?ы븿
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final FpsResponseCodeResolver responseCodeResolver;

    public GlobalExceptionHandler(ObjectProvider<FpsResponseCodeResolver> responseCodeResolverProvider) {
        this.responseCodeResolver = responseCodeResolverProvider.getIfAvailable(DefaultFpsResponseCodeResolver::new);
    }

    /**
     * PFW ?쒖? ?덉쇅 泥섎━
     * 嫄곕옒 ?ㅻ뜑, 嫄곕옒 硫뷀??곗씠?? PFW 怨듯넻 湲곕뒫?먯꽌 諛쒖깮???ㅻ쪟??PFW ?쒖? ?ㅻ쪟 ?묐떟?쇰줈 諛섑솚?⑸땲??
     */
    @ExceptionHandler(FpsException.class)
    public ResponseEntity<FpsErrorResponse> handleFpsException(FpsException ex, WebRequest request) {
        FpsResolvedResponse resolvedResponse = ex.getErrorCode() != null
                ? responseCodeResolver.resolve(ex.getErrorCode(), java.util.Locale.KOREAN, ex.getMessageArguments(), ex.getDetail())
                : responseCodeResolver.resolve(ex.getResponseCode(), java.util.Locale.KOREAN, ex.getMessageArguments(), ex.getDetail());
        String externalMessage = firstText(ex.getExternalMessage(), resolvedResponse.externalMessage());

        log.warn("PFW Exception [{}] - Message: {}, Details: {}",
                resolvedResponse.messageCode(),
                externalMessage,
                ex.getDetail());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Code", resolvedResponse.errorCode());
        headers.add("X-Message-Code", resolvedResponse.messageCode());
        headers.add("X-Fps-Response-Code", resolvedResponse.responseCode());
        headers.add("X-Fps-Response-Message-Code", resolvedResponse.messageCode());
        headers.add("X-Fps-Message-Code", resolvedResponse.messageCode());
        headers.add("X-Error-Type", ex.getClass().getSimpleName());

        FpsErrorResponse response = FpsErrorResponse.of(
                resolvedResponse,
                externalMessage,
                ex.getClass().getSimpleName(),
                null);

        return ResponseEntity.status(resolvedResponse.httpStatus())
                .headers(headers)
                .body(response);
    }
    
    /**
     * ApiException 泥섎━
     * 鍮꾩쫰?덉뒪 濡쒖쭅?먯꽌 諛쒖깮???덉쇅
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<BaseResponse<?>> handleApiException(
            ApiException ex,
            WebRequest request) {
        
        // 濡쒓퉭: 鍮꾩쫰?덉뒪 ?덉쇅 (?뺤긽 踰붿쐞???덉쇅)
        log.warn("API Exception [{}] - Message: {}, Details: {}",
                ex.getResponseCode().getCode(),
                ex.getErrorMessage(),
                ex.getDetails());
        
        BaseResponse<?> response = BaseResponse.error(
                ex.getResponseCode(),
                ex.getErrorMessage()
        );
        
        // ?곹깭 肄붾뱶 寃곗젙
        HttpStatus httpStatus = determineHttpStatus(ex.getResponseCode());
        
        return new ResponseEntity<>(response, httpStatus);
    }
    
    /**
     * ?낅젰媛?寃利??ㅻ쪟 泥섎━
     * @Valid 寃利??ㅽ뙣??     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        // 泥?踰덉㎏ 寃利??ㅻ쪟 ?꾨뱶 異붿텧
        String fieldName = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("?낅젰媛?寃利??ㅽ뙣");
        
        log.warn("Validation Exception - Field: {}", fieldName);
        
        BaseResponse.ErrorDetail errorDetail = BaseResponse.ErrorDetail.builder()
                .errorType("VALIDATION_ERROR")
                .fieldName(fieldName)
                .details("?낅젰??媛믪씠 ?좏슚?섏? ?딆뒿?덈떎.")
                .build();
        
        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.VALIDATION_FAILED,
                errorDetail
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 荑쇰━ ?뚮씪誘명꽣/RequestParam 寃利??ㅻ쪟 泥섎━
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<?>> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request) {

        String details = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .orElse("?뚮씪誘명꽣 寃利??ㅽ뙣");

        log.warn("Constraint Violation Exception - {}", details);

        BaseResponse.ErrorDetail errorDetail = BaseResponse.ErrorDetail.builder()
                .errorType("PARAMETER_VALIDATION_ERROR")
                .fieldName(details)
                .details("?붿껌 ?뚮씪誘명꽣媛 ?좏슚?섏? ?딆뒿?덈떎.")
                .build();

        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.VALIDATION_FAILED,
                errorDetail
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * ?꾩닔 荑쇰━ ?뚮씪誘명꽣 ?꾨씫 泥섎━
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<?>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            WebRequest request) {

        String fieldName = ex.getParameterName();
        log.warn("Missing Request Parameter - {}", fieldName);

        BaseResponse.ErrorDetail errorDetail = BaseResponse.ErrorDetail.builder()
                .errorType("MISSING_PARAMETER")
                .fieldName(fieldName)
                .details("?꾩닔 ?붿껌 ?뚮씪誘명꽣媛 ?꾨씫?섏뿀?듬땲??")
                .build();

        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.INVALID_PARAMETER,
                errorDetail
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 荑쇰━ ?뚮씪誘명꽣 ???蹂???ㅻ쪟 泥섎━
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {

        log.warn("Method Argument Type Mismatch - field: {}, value: {}",
                ex.getName(),
                ex.getValue());

        BaseResponse.ErrorDetail errorDetail = BaseResponse.ErrorDetail.builder()
                .errorType("PARAMETER_TYPE_MISMATCH")
                .fieldName(ex.getName())
                .details("?붿껌 ?뚮씪誘명꽣 ??낆씠 ?щ컮瑜댁? ?딆뒿?덈떎.")
                .build();

        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.INVALID_PARAMETER,
                errorDetail
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * JSON Body ?뚯떛 ?ㅻ쪟 泥섎━
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<?>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        log.warn("HTTP Message Not Readable: {}", ex.getMessage());

        BaseResponse.ErrorDetail errorDetail = BaseResponse.ErrorDetail.builder()
                .errorType("INVALID_REQUEST_BODY")
                .fieldName("body")
                .details("?붿껌 蹂몃Ц ?뺤떇???щ컮瑜댁? ?딆뒿?덈떎.")
                .build();

        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.BAD_REQUEST,
                errorDetail
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 留ㅽ븨?섏? ?딆? URL/?뺤쟻 由ъ냼???붿껌 泥섎━
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<BaseResponse<?>> handleNotFoundException(
            Exception ex,
            WebRequest request) {

        log.warn("Resource Not Found: {}", ex.getMessage());

        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.NOT_FOUND,
                "?붿껌??API瑜?李얠쓣 ???놁뒿?덈떎."
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    /**
     * IllegalArgumentException 泥섎━
     * ?좏슚?섏? ?딆? ?뚮씪誘명꽣
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<?>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {
        
        log.warn("Illegal Argument Exception: {}", ex.getMessage());
        
        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.INVALID_PARAMETER,
                ex.getMessage()
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * ?덉긽移?紐삵븳 ?쒕쾭 ?ㅻ쪟 泥섎━
     * 紐⑤뱺 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<?>> handleRuntimeException(
            RuntimeException ex,
            WebRequest request) {
        
        log.error("Unexpected Runtime Exception", ex);
        
        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.INTERNAL_SERVER_ERROR,
                "?쒕쾭 泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎."
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 紐⑤뱺 ?덉쇅 泥섎━ (Fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(
            Exception ex,
            WebRequest request) {
        
        log.error("Unexpected Exception", ex);
        
        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.INTERNAL_SERVER_ERROR,
                "?쒖뒪???ㅻ쪟媛 諛쒖깮?덉뒿?덈떎."
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * ?묐떟 肄붾뱶???곕씪 HTTP ?곹깭 肄붾뱶 寃곗젙
     */
    private HttpStatus determineHttpStatus(ResponseCode responseCode) {
        return switch (responseCode) {
            case SUCCESS, CREATED, UPDATED, DELETED -> HttpStatus.OK;
            case BAD_REQUEST, INVALID_PARAMETER, VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private String firstText(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }
}

