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
 * 湲濡쒕쾶 ?덉쇅 泥섎━ ?몃뱾???대옒??
 * - MVC ?덈꺼??二쇱슂 ?덉쇅瑜?泥섎━?섍퀬 DB???몃옖??뀡 濡쒓렇瑜?湲곕줉?⑸땲??
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ApplicationEventPublisher eventPublisher;

    /**
     * ?앹꽦??
     *
     * @param eventPublisher ?대깽??諛쒗뻾??(?몃옖??뀡 濡쒓렇 ??μ슜)
     */
    public GlobalExceptionHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * MissingServletRequestParameterException 泥섎━.
     *
     * @param ex      ?꾨씫???뚮씪誘명꽣 ?덉쇅
     * @param request HTTP ?붿껌 媛앹껜
     * @return ?대씪?댁뼵?몄뿉 諛섑솚??HTTP ?묐떟
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex, WebRequest request) {
        return processExceptionAndLog(ex, request, HttpStatus.BAD_REQUEST, "FAILURE");
    }

    /**
     * ConstraintViolationException 泥섎━.
     *
     * @param ex      ?좏슚??寃利??ㅽ뙣 ?덉쇅
     * @param request HTTP ?붿껌 媛앹껜
     * @return ?대씪?댁뼵?몄뿉 諛섑솚??HTTP ?묐떟
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
     * IllegalArgumentException 泥섎━.
     *
     * @param ex      IllegalArgumentException ?덉쇅
     * @param request HTTP ?붿껌 媛앹껜
     * @return ?대씪?댁뼵?몄뿉 諛섑솚??HTTP ?묐떟
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return processExceptionAndLog(ex, request, HttpStatus.BAD_REQUEST, "FAILURE");
    }

    /**
     * ?쇰컲?곸씤 ?덉쇅 泥섎━.
     *
     * @param ex      諛쒖깮???덉쇅
     * @param request HTTP ?붿껌 媛앹껜
     * @return ?대씪?댁뼵?몄뿉 諛섑솚??HTTP ?묐떟
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex, WebRequest request) {
        return processExceptionAndLog(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, "FAILURE");
    }

    /**
     * ?ㅻ쪟 硫붿떆吏瑜??щ㎎?섍퀬 ?몃옖??뀡 濡쒓렇瑜?湲곕줉?⑸땲??
     *
     * @param ex      ?덉쇅 媛앹껜
     * @param request HTTP ?붿껌 媛앹껜
     * @param status  HTTP ?곹깭 肄붾뱶
     * @param logType 濡쒓렇 ?좏삎
     * @return ?대씪?댁뼵?몄뿉 諛섑솚??HTTP ?묐떟
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
                null, // ?붿껌 ?뚮씪誘명꽣 ?놁쓬
                null, // ?묐떟 ?곗씠???놁쓬
                status.value(),
                ex.getMessage(),
                execUser,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body("Error: " + SensitiveDataMasker.mask(ex.getMessage()));
    }

    /**
     * ?몃옖??뀡 濡쒓렇 諛쒗뻾 硫붿꽌??
     *
     * @param transactionId ?몃옖??뀡 ID
     * @param traceId       Trace ID
     * @param spanId        Span ID
     * @param parentSpanId  Parent Span ID
     * @param logType       濡쒓렇 ?좏삎 (SUCCESS/FAILURE)
     * @param moduleId      ⑤뱢 ID
     * @param menuId        硫붾돱 ID
     * @param uri           ?붿껌 URI
     * @param parameters    ?붿껌 ?뚮씪誘명꽣
     * @param response      ?묐떟 ?곗씠??
     * @param responseCode  HTTP ?묐떟 肄붾뱶
     * @param errorMessage  ?ㅻ쪟 硫붿떆吏
     * @param execUser      ?ㅽ뻾 ?ъ슜??
     * @param startTime     ?붿껌 ?쒖옉 ?쒓컙
     * @param endTime       ?붿껌 醫낅즺 ?쒓컙
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
     * ConstraintViolation ?뺣낫瑜??щ㎎?낇빀?덈떎.
     *
     * @param violation ?좏슚??寃利??ㅽ뙣 ?뺣낫
     * @return ?щ㎎???ㅻ쪟 硫붿떆吏
     */
    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }

    /**
     * ?붿껌 URI瑜?諛섑솚?⑸땲??
     *
     * @param request WebRequest 媛앹껜
     * @return ?붿껌 URI
     */
    private String getRequestUri(WebRequest request) {
        return Optional.ofNullable(request.getDescription(false)).orElse("Unknown URI");
    }
}

