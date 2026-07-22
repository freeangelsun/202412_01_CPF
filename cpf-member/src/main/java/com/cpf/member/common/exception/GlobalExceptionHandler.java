package cpf.mbr.common.exception;

import cpf.mbr.common.response.BaseResponse;
import cpf.mbr.common.response.ResponseCode;
import cpf.pfw.common.exception.CpfErrorResponse;
import cpf.pfw.common.exception.CpfException;
import cpf.pfw.common.exception.CpfResolvedResponse;
import cpf.pfw.common.exception.CpfResponseCodeResolver;
import cpf.pfw.common.exception.DefaultCpfResponseCodeResolver;
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
 * MBR 전역 예외 처리기입니다.
 * MVC validation, 파라미터 오류, CPF 공통 예외를 MBR 표준 응답으로 변환합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final CpfResponseCodeResolver responseCodeResolver;

    public GlobalExceptionHandler(ObjectProvider<CpfResponseCodeResolver> responseCodeResolverProvider) {
        this.responseCodeResolver = responseCodeResolverProvider.getIfAvailable(DefaultCpfResponseCodeResolver::new);
    }

    @ExceptionHandler(CpfException.class)
    public ResponseEntity<CpfErrorResponse> handleCpfException(CpfException ex, WebRequest request) {
        CpfResolvedResponse resolvedResponse = ex.getErrorCode() != null
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
        headers.add("X-Cpf-Response-Code", resolvedResponse.responseCode());
        headers.add("X-Cpf-Response-Message-Code", resolvedResponse.messageCode());
        headers.add("X-Cpf-Message-Code", resolvedResponse.messageCode());
        headers.add("X-Error-Type", ex.getClass().getSimpleName());

        CpfErrorResponse response = CpfErrorResponse.of(
                resolvedResponse,
                externalMessage,
                ex.getClass().getSimpleName(),
                null);

        return ResponseEntity.status(resolvedResponse.httpStatus())
                .headers(headers)
                .body(response);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<BaseResponse<?>> handleApiException(ApiException ex, WebRequest request) {
        log.warn("API Exception [{}] - Message: {}, Details: {}",
                ex.getResponseCode().getCode(),
                ex.getErrorMessage(),
                ex.getDetails());
        return new ResponseEntity<>(
                BaseResponse.error(ex.getResponseCode(), ex.getErrorMessage()),
                determineHttpStatus(ex.getResponseCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String fieldName = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("요청 본문 검증에 실패했습니다.");
        log.warn("Validation Exception - Field: {}", fieldName);
        return badRequest(ResponseCode.VALIDATION_FAILED, "VALIDATION_ERROR", fieldName, "요청 본문 검증에 실패했습니다.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<?>> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        String details = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .orElse("요청 파라미터 검증에 실패했습니다.");
        log.warn("Constraint Violation Exception - {}", details);
        return badRequest(ResponseCode.VALIDATION_FAILED, "PARAMETER_VALIDATION_ERROR", details, "요청 파라미터 검증에 실패했습니다.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<?>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            WebRequest request) {
        log.warn("Missing Request Parameter - {}", ex.getParameterName());
        return badRequest(ResponseCode.INVALID_PARAMETER, "MISSING_PARAMETER", ex.getParameterName(), "필수 요청 파라미터가 누락되었습니다.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        log.warn("Method Argument Type Mismatch - field: {}, value: {}", ex.getName(), ex.getValue());
        return badRequest(ResponseCode.INVALID_PARAMETER, "PARAMETER_TYPE_MISMATCH", ex.getName(), "요청 파라미터 타입이 올바르지 않습니다.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<?>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            WebRequest request) {
        log.warn("HTTP Message Not Readable: {}", ex.getMessage());
        return badRequest(ResponseCode.BAD_REQUEST, "INVALID_REQUEST_BODY", "body", "요청 본문을 읽을 수 없습니다.");
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<BaseResponse<?>> handleNotFoundException(Exception ex, WebRequest request) {
        log.warn("Resource Not Found: {}", ex.getMessage());
        return new ResponseEntity<>(BaseResponse.error(ResponseCode.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal Argument Exception: {}", ex.getMessage());
        return new ResponseEntity<>(BaseResponse.error(ResponseCode.INVALID_PARAMETER, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<?>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Unexpected Runtime Exception", ex);
        return new ResponseEntity<>(
                BaseResponse.error(ResponseCode.INTERNAL_SERVER_ERROR, "처리 중 오류가 발생했습니다."),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(Exception ex, WebRequest request) {
        log.error("Unexpected Exception", ex);
        return new ResponseEntity<>(
                BaseResponse.error(ResponseCode.INTERNAL_SERVER_ERROR, "처리 중 오류가 발생했습니다."),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<BaseResponse<?>> badRequest(
            ResponseCode responseCode,
            String errorType,
            String fieldName,
            String details) {
        BaseResponse.ErrorDetail errorDetail = BaseResponse.ErrorDetail.builder()
                .errorType(errorType)
                .fieldName(fieldName)
                .details(details)
                .build();
        return new ResponseEntity<>(BaseResponse.error(responseCode, errorDetail), HttpStatus.BAD_REQUEST);
    }

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
