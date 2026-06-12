package fps.mbr.common.exception;

import fps.mbr.common.response.BaseResponse;
import fps.mbr.common.response.ResponseCode;
import fps.pfw.common.exception.FpsErrorDefinition;
import fps.pfw.common.exception.FpsErrorResponse;
import fps.pfw.common.exception.FpsException;
import fps.pfw.common.exception.FpsMessageFormatter;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
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
 * 금융권 글로벌 예외 처리 핸들러
 * - 모든 API에서 발생하는 예외를 통일된 형식으로 처리
 * - 예외 로깅 및 감시 기능 포함
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * PFW 표준 예외 처리
     * 거래 헤더, 거래 메타데이터, PFW 공통 기능에서 발생한 오류는 PFW 표준 오류 응답으로 반환합니다.
     */
    @ExceptionHandler(FpsException.class)
    public ResponseEntity<FpsErrorResponse> handleFpsException(FpsException ex, WebRequest request) {
        FpsErrorDefinition errorCode = ex.getErrorCode();
        String externalTemplate = firstText(ex.getExternalMessage(), errorCode.getDefaultExternalMessage());
        String externalMessage = FpsMessageFormatter.format(externalTemplate, ex.getMessageArguments());

        log.warn("PFW Exception [{}] - Message: {}, Details: {}",
                errorCode.getMessageCode(),
                externalMessage,
                ex.getDetail());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Code", errorCode.getStatusCode());
        headers.add("X-Message-Code", errorCode.getMessageCode());
        headers.add("X-Error-Type", ex.getClass().getSimpleName());

        FpsErrorResponse response = FpsErrorResponse.of(
                errorCode,
                externalMessage,
                ex.getClass().getSimpleName(),
                null);

        return ResponseEntity.status(errorCode.getHttpStatus())
                .headers(headers)
                .body(response);
    }
    
    /**
     * ApiException 처리
     * 비즈니스 로직에서 발생한 예외
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<BaseResponse<?>> handleApiException(
            ApiException ex,
            WebRequest request) {
        
        // 로깅: 비즈니스 예외 (정상 범위의 예외)
        log.warn("API Exception [{}] - Message: {}, Details: {}",
                ex.getResponseCode().getCode(),
                ex.getErrorMessage(),
                ex.getDetails());
        
        BaseResponse<?> response = BaseResponse.error(
                ex.getResponseCode(),
                ex.getErrorMessage()
        );
        
        // 상태 코드 결정
        HttpStatus httpStatus = determineHttpStatus(ex.getResponseCode());
        
        return new ResponseEntity<>(response, httpStatus);
    }
    
    /**
     * 입력값 검증 오류 처리
     * @Valid 검증 실패시
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        // 첫 번째 검증 오류 필드 추출
        String fieldName = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("입력값 검증 실패");
        
        log.warn("Validation Exception - Field: {}", fieldName);
        
        BaseResponse.ErrorDetail errorDetail = BaseResponse.ErrorDetail.builder()
                .errorType("VALIDATION_ERROR")
                .fieldName(fieldName)
                .details("입력된 값이 유효하지 않습니다.")
                .build();
        
        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.VALIDATION_FAILED,
                errorDetail
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 쿼리 파라미터/RequestParam 검증 오류 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<?>> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request) {

        String details = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .orElse("파라미터 검증 실패");

        log.warn("Constraint Violation Exception - {}", details);

        BaseResponse.ErrorDetail errorDetail = BaseResponse.ErrorDetail.builder()
                .errorType("PARAMETER_VALIDATION_ERROR")
                .fieldName(details)
                .details("요청 파라미터가 유효하지 않습니다.")
                .build();

        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.VALIDATION_FAILED,
                errorDetail
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 필수 쿼리 파라미터 누락 처리
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
                .details("필수 요청 파라미터가 누락되었습니다.")
                .build();

        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.INVALID_PARAMETER,
                errorDetail
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 쿼리 파라미터 타입 변환 오류 처리
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
                .details("요청 파라미터 타입이 올바르지 않습니다.")
                .build();

        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.INVALID_PARAMETER,
                errorDetail
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * JSON Body 파싱 오류 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<?>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        log.warn("HTTP Message Not Readable: {}", ex.getMessage());

        BaseResponse.ErrorDetail errorDetail = BaseResponse.ErrorDetail.builder()
                .errorType("INVALID_REQUEST_BODY")
                .fieldName("body")
                .details("요청 본문 형식이 올바르지 않습니다.")
                .build();

        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.BAD_REQUEST,
                errorDetail
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 매핑되지 않은 URL/정적 리소스 요청 처리
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<BaseResponse<?>> handleNotFoundException(
            Exception ex,
            WebRequest request) {

        log.warn("Resource Not Found: {}", ex.getMessage());

        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.NOT_FOUND,
                "요청한 API를 찾을 수 없습니다."
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    /**
     * IllegalArgumentException 처리
     * 유효하지 않은 파라미터
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
     * 예상치 못한 서버 오류 처리
     * 모든 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<?>> handleRuntimeException(
            RuntimeException ex,
            WebRequest request) {
        
        log.error("Unexpected Runtime Exception", ex);
        
        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.INTERNAL_SERVER_ERROR,
                "서버 처리 중 오류가 발생했습니다."
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 모든 예외 처리 (Fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(
            Exception ex,
            WebRequest request) {
        
        log.error("Unexpected Exception", ex);
        
        BaseResponse<?> response = BaseResponse.error(
                ResponseCode.INTERNAL_SERVER_ERROR,
                "시스템 오류가 발생했습니다."
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 응답 코드에 따라 HTTP 상태 코드 결정
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
