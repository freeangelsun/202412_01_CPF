package cpf.acc.common.exception;

import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionHeader;
import cpf.pfw.common.logging.TransactionLogEvent;
import cpf.pfw.common.logging.TransactionLogRecord;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ACC API에서 발생한 예외를 공통 오류 응답과 PFW 거래 로그로 변환합니다.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ApplicationEventPublisher eventPublisher;

    public GlobalExceptionHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 필수 요청 파라미터가 누락된 경우 400 오류로 응답합니다.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex, WebRequest request) {
        return processExceptionAndLog(ex, request, HttpStatus.BAD_REQUEST, "FAILURE");
    }

    /**
     * Bean Validation 오류를 필드별 메시지로 묶어 400 오류로 응답합니다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleValidationException(ConstraintViolationException ex, WebRequest request) {
        String errorDetails = ex.getConstraintViolations().stream()
                .map(this::formatConstraintViolation)
                .collect(Collectors.joining(", "));

        logger.error("Validation failed: {}, Request Details: {}", errorDetails, getRequestUri(request));

        return processExceptionAndLog(
                new Exception("Validation failed: " + errorDetails),
                request,
                HttpStatus.BAD_REQUEST,
                "FAILURE"
        );
    }

    /**
     * 잘못된 인자 오류를 400 오류로 응답합니다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return processExceptionAndLog(ex, request, HttpStatus.BAD_REQUEST, "FAILURE");
    }

    /**
     * 처리되지 않은 예외를 500 오류로 응답합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex, WebRequest request) {
        return processExceptionAndLog(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, "FAILURE");
    }

    /**
     * 오류 응답을 생성하고 거래 로그 이벤트를 발행합니다.
     */
    private ResponseEntity<String> processExceptionAndLog(Exception ex, WebRequest request, HttpStatus status, String logType) {
        String requestUri = getRequestUri(request);
        String execUser = "Unknown User";

        logger.error("Error occurred: {}, Request Details: {}", ex.getMessage(), requestUri);

        publishTransactionLog(
                TransactionContext.getOrCreateTransactionId(),
                TransactionContext.getOrCreateTraceId(),
                TransactionContext.getOrCreateSpanId(),
                TransactionContext.currentParentSpanId(),
                logType,
                "ACC",
                "default-menu",
                requestUri,
                null,
                null,
                status.value(),
                ex.getMessage(),
                execUser,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body("Error: " + SensitiveDataMasker.mask(ex.getMessage()));
    }

    /**
     * PFW 거래 로그에 저장할 오류 거래 레코드를 구성합니다.
     *
     * @param transactionId 거래 ID
     * @param traceId       Trace ID
     * @param spanId        Span ID
     * @param parentSpanId  Parent Span ID
     * @param logType       로그 유형
     * @param moduleId      모듈 ID
     * @param menuId        메뉴 ID
     * @param uri           요청 URI
     * @param parameters    요청 파라미터
     * @param response      응답 본문
     * @param responseCode  HTTP 응답 코드
     * @param errorMessage  오류 메시지
     * @param execUser      실행 사용자
     * @param startTime     시작 일시
     * @param endTime       종료 일시
     */
    private void publishTransactionLog(String transactionId, String traceId, String spanId, String parentSpanId,
                                       String logType, String moduleId, String menuId,
                                       String uri, String parameters, String response,
                                       int responseCode, String errorMessage, String execUser,
                                       LocalDateTime startTime, LocalDateTime endTime) {
        String maskedErrorMessage = Optional.ofNullable(SensitiveDataMasker.mask(errorMessage)).orElse("N/A");
        TransactionHeader transactionHeader = TransactionContext.currentHeader();
        TransactionLogRecord record = TransactionLogRecord.builder()
                .transactionId(transactionId)
                .traceId(traceId)
                .spanId(spanId)
                .parentSpanId(parentSpanId)
                .sequenceNo(TransactionContext.nextSequenceNo())
                .moduleId(moduleId)
                .menuId(menuId)
                .businessTransactionId("ACC99ERR0001")
                .businessTransactionName("ACC 공통 예외 처리")
                .logType(logType)
                .apiVersion(headerValue(transactionHeader, TransactionHeader::getApiVersion, null))
                .clientAppId(headerValue(transactionHeader, TransactionHeader::getClientAppId, null))
                .clientVersion(headerValue(transactionHeader, TransactionHeader::getClientVersion, null))
                .callerService(headerValue(transactionHeader, TransactionHeader::getCallerService, null))
                .callerInstanceId(headerValue(transactionHeader, TransactionHeader::getCallerInstanceId, null))
                .correlationId(headerValue(transactionHeader, TransactionHeader::getCorrelationId, null))
                .idempotencyKey(headerValue(transactionHeader, TransactionHeader::getIdempotencyKey, null))
                .locale(headerValue(transactionHeader, TransactionHeader::getLocale, null))
                .timezone(headerValue(transactionHeader, TransactionHeader::getTimezone, null))
                .requestType(headerValue(transactionHeader, TransactionHeader::getRequestType, "UNKNOWN"))
                .originalChannelCode(headerValue(transactionHeader, TransactionHeader::getOriginalChannelCode, "UNKNOWN"))
                .channelCode(headerValue(transactionHeader, TransactionHeader::getChannelCode, "UNKNOWN"))
                .memberNo(headerValue(transactionHeader, TransactionHeader::getMemberNo, null))
                .customerNo(headerValue(transactionHeader, TransactionHeader::getCustomerNo, null))
                .screenId(headerValue(transactionHeader, TransactionHeader::getScreenId, null))
                .deviceId(headerValue(transactionHeader, TransactionHeader::getDeviceId, null))
                .clientRequestTime(headerValue(transactionHeader, TransactionHeader::getClientRequestTime, null))
                .clientIp(headerValue(transactionHeader, TransactionHeader::getClientIp, null))
                .wasId(headerValue(transactionHeader, TransactionHeader::getWasId, "UNKNOWN"))
                .reservedField1(headerValue(transactionHeader, TransactionHeader::getReservedField1, null))
                .reservedField2(headerValue(transactionHeader, TransactionHeader::getReservedField2, null))
                .reservedField3(headerValue(transactionHeader, TransactionHeader::getReservedField3, null))
                .reservedField4(headerValue(transactionHeader, TransactionHeader::getReservedField4, null))
                .reservedField5(headerValue(transactionHeader, TransactionHeader::getReservedField5, null))
                .uri(uri)
                .parameters(SensitiveDataMasker.mask(parameters))
                .response(SensitiveDataMasker.mask(response))
                .responseCode(String.valueOf(responseCode))
                .errorMessage(maskedErrorMessage)
                .execUser(execUser)
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(0L)
                .build();

        TransactionLogEvent event = new TransactionLogEvent(
                this,
                record,
                Map.of("error", maskedErrorMessage)
        );
        eventPublisher.publishEvent(event);
    }

    private String headerValue(TransactionHeader transactionHeader,
                               java.util.function.Function<TransactionHeader, String> accessor,
                               String fallback) {
        if (transactionHeader == null) {
            return fallback;
        }
        String value = accessor.apply(transactionHeader);
        return value != null && !value.isBlank() ? value : fallback;
    }

    /**
     * Bean Validation 위반 정보를 사람이 읽을 수 있는 문자열로 변환합니다.
     */
    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }

    /**
     * WebRequest 설명에서 요청 URI를 추출합니다.
     */
    private String getRequestUri(WebRequest request) {
        return Optional.ofNullable(request.getDescription(false)).orElse("Unknown URI");
    }
}
