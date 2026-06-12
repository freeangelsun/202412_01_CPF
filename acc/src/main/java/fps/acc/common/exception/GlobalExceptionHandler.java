package fps.acc.common.exception;

import fps.pfw.common.logging.SensitiveDataMasker;
import fps.pfw.common.logging.TransactionContext;
import fps.pfw.common.logging.TransactionHeader;
import fps.pfw.common.logging.TransactionLogEvent;
import fps.pfw.common.logging.TransactionLogRecord;
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
 * 글로벌 예외 처리 핸들러 클래스.
 * - MVC 레벨의 주요 예외를 처리하고 DB에 트랜잭션 로그를 기록합니다.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 생성자.
     *
     * @param eventPublisher 이벤트 발행자 (트랜잭션 로그 저장용)
     */
    public GlobalExceptionHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * MissingServletRequestParameterException 처리.
     *
     * @param ex      누락된 파라미터 예외
     * @param request HTTP 요청 객체
     * @return 클라이언트에 반환할 HTTP 응답
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex, WebRequest request) {
        return processExceptionAndLog(ex, request, HttpStatus.BAD_REQUEST, "FAILURE");
    }

    /**
     * ConstraintViolationException 처리.
     *
     * @param ex      유효성 검증 실패 예외
     * @param request HTTP 요청 객체
     * @return 클라이언트에 반환할 HTTP 응답
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
     * IllegalArgumentException 처리.
     *
     * @param ex      IllegalArgumentException 예외
     * @param request HTTP 요청 객체
     * @return 클라이언트에 반환할 HTTP 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return processExceptionAndLog(ex, request, HttpStatus.BAD_REQUEST, "FAILURE");
    }

    /**
     * 일반적인 예외 처리.
     *
     * @param ex      발생한 예외
     * @param request HTTP 요청 객체
     * @return 클라이언트에 반환할 HTTP 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex, WebRequest request) {
        return processExceptionAndLog(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, "FAILURE");
    }

    /**
     * 오류 메시지를 포맷하고 트랜잭션 로그를 기록합니다.
     *
     * @param ex      예외 객체
     * @param request HTTP 요청 객체
     * @param status  HTTP 상태 코드
     * @param logType 로그 유형
     * @return 클라이언트에 반환할 HTTP 응답
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
                null, // 요청 파라미터 없음
                null, // 응답 데이터 없음
                status.value(),
                ex.getMessage(),
                execUser,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body("Error: " + SensitiveDataMasker.mask(ex.getMessage()));
    }

    /**
     * 트랜잭션 로그 발행 메서드.
     *
     * @param transactionId 트랜잭션 ID
     * @param traceId       Trace ID
     * @param spanId        Span ID
     * @param parentSpanId  Parent Span ID
     * @param logType       로그 유형 (SUCCESS/FAILURE)
     * @param moduleId      모듈 ID
     * @param menuId        메뉴 ID
     * @param uri           요청 URI
     * @param parameters    요청 파라미터
     * @param response      응답 데이터
     * @param responseCode  HTTP 응답 코드
     * @param errorMessage  오류 메시지
     * @param execUser      실행 사용자
     * @param startTime     요청 시작 시간
     * @param endTime       요청 종료 시간
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
                .businessTransactionName("ACC공통예외처리")
                .logType(logType)
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
                .responseCode(responseCode)
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
     * ConstraintViolation 정보를 포맷팅합니다.
     *
     * @param violation 유효성 검증 실패 정보
     * @return 포맷된 오류 메시지
     */
    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }

    /**
     * 요청 URI를 반환합니다.
     *
     * @param request WebRequest 객체
     * @return 요청 URI
     */
    private String getRequestUri(WebRequest request) {
        return Optional.ofNullable(request.getDescription(false)).orElse("Unknown URI");
    }
}
