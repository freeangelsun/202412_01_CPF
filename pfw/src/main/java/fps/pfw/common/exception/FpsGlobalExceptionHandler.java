package fps.pfw.common.exception;

import fps.pfw.common.logging.SensitiveDataMasker;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

/**
 * FPS 표준 예외를 공통 오류 응답으로 변환하는 프레임워크 핸들러입니다.
 *
 * <p>업무 개발자는 컨트롤러에서 try/catch를 반복하지 않고
 * {@link FpsException} 계열 예외를 던지면 됩니다. 이 핸들러가 오류코드,
 * 고객용 메시지, 거래ID/TraceId, 응답 헤더를 표준 형식으로 내려보냅니다.</p>
 */
@Order(-100)
@RestControllerAdvice
public class FpsGlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(FpsGlobalExceptionHandler.class);

    private final FpsMessageResolver messageResolver;

    public FpsGlobalExceptionHandler(ObjectProvider<FpsMessageResolver> messageResolverProvider) {
        this.messageResolver = messageResolverProvider.getIfAvailable(DefaultFpsMessageResolver::new);
    }

    /**
     * FPS 표준 예외를 처리합니다.
     *
     * @param ex      업무/프레임워크에서 던진 표준 예외
     * @param request 현재 HTTP 요청
     * @return 고객에게 반환할 표준 오류 응답
     */
    @ExceptionHandler(FpsException.class)
    public ResponseEntity<FpsErrorResponse> handleFpsException(FpsException ex, HttpServletRequest request) {
        FpsErrorDefinition errorCode = ex.getErrorCode();
        FpsResolvedMessage resolvedMessage = messageResolver.resolve(errorCode, request.getLocale());
        String externalTemplate = firstText(ex.getExternalMessage(), resolvedMessage.externalMessage(), errorCode.getDefaultExternalMessage());
        String internalTemplate = firstText(ex.getInternalMessage(), resolvedMessage.internalMessage(), errorCode.getDefaultInternalMessage());
        String externalMessage = FpsMessageFormatter.format(externalTemplate, ex.getMessageArguments());
        String internalMessage = FpsMessageFormatter.format(internalTemplate, ex.getMessageArguments());

        log.warn(
                "FPS exception handled. statusCode={}, messageCode={}, externalMessage={}, internalMessage={}, detail={}, uri={}",
                errorCode.getStatusCode(),
                errorCode.getMessageCode(),
                externalMessage,
                SensitiveDataMasker.mask(internalMessage),
                SensitiveDataMasker.mask(ex.getDetail()),
                request.getRequestURI(),
                ex);

        FpsErrorResponse response = FpsErrorResponse.of(
                errorCode,
                externalMessage,
                ex.getClass().getSimpleName(),
                null);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Code", errorCode.getStatusCode());
        headers.add("X-Message-Code", errorCode.getMessageCode());
        headers.add("X-Error-Type", ex.getClass().getSimpleName());

        return ResponseEntity.status(errorCode.getHttpStatus())
                .headers(headers)
                .body(response);
    }

    private String firstText(String first, String second, String third) {
        if (hasText(first)) {
            return first;
        }
        if (hasText(second)) {
            return second;
        }
        return third;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
