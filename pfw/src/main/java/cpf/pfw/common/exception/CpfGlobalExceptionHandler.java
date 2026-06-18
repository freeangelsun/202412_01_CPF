package cpf.pfw.common.exception;

import cpf.pfw.common.logging.SensitiveDataMasker;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * CPF 공통 예외를 표준 오류 응답으로 변환합니다.
 *
 * 운영 로그에는 내부 메시지와 상세 사유를 마스킹해 남기고, 클라이언트에는 외부 메시지만 반환합니다.
 */
@Order(-100)
@RestControllerAdvice
public class CpfGlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CpfGlobalExceptionHandler.class);

    private final CpfMessageResolver messageResolver;
    private final CpfResponseCodeResolver responseCodeResolver;

    public CpfGlobalExceptionHandler(
            ObjectProvider<CpfMessageResolver> messageResolverProvider,
            ObjectProvider<CpfResponseCodeResolver> responseCodeResolverProvider) {
        this.messageResolver = messageResolverProvider.getIfAvailable(DefaultCpfMessageResolver::new);
        this.responseCodeResolver = responseCodeResolverProvider.getIfAvailable(DefaultCpfResponseCodeResolver::new);
    }

    /**
     * CPF 예외에서 응답코드, 메시지코드, 외부 메시지를 해석해 HTTP 응답을 생성합니다.
     */
    @ExceptionHandler(CpfException.class)
    public ResponseEntity<CpfErrorResponse> handleCpfException(CpfException ex, HttpServletRequest request) {
        CpfErrorDefinition errorCode = ex.getErrorCode();
        CpfResolvedResponse resolvedResponse = errorCode != null
                ? responseCodeResolver.resolve(errorCode, request.getLocale(), ex.getMessageArguments(), ex.getDetail())
                : responseCodeResolver.resolve(ex.getResponseCode(), request.getLocale(), ex.getMessageArguments(), ex.getDetail());
        String externalMessage = firstText(ex.getExternalMessage(), resolvedResponse.externalMessage());
        String internalMessage = firstText(ex.getInternalMessage(), resolvedResponse.internalMessage());

        log.warn(
                "CPF exception handled. statusCode={}, messageCode={}, externalMessage={}, internalMessage={}, detail={}, uri={}",
                resolvedResponse.responseCode(),
                resolvedResponse.messageCode(),
                externalMessage,
                SensitiveDataMasker.mask(internalMessage),
                SensitiveDataMasker.mask(ex.getDetail()),
                request.getRequestURI(),
                ex);

        CpfErrorResponse response = CpfErrorResponse.of(
                resolvedResponse,
                externalMessage,
                ex.getClass().getSimpleName(),
                null);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Code", resolvedResponse.errorCode());
        headers.add("X-Message-Code", resolvedResponse.messageCode());
        headers.add("X-Cpf-Response-Code", resolvedResponse.responseCode());
        headers.add("X-Cpf-Response-Message-Code", resolvedResponse.messageCode());
        headers.add("X-Cpf-Message-Code", resolvedResponse.messageCode());
        headers.add("X-Error-Type", ex.getClass().getSimpleName());

        return ResponseEntity.status(resolvedResponse.httpStatus())
                .headers(headers)
                .body(response);
    }

    /**
     * 메시지 우선순위에 따라 먼저 값이 있는 문자열을 선택합니다.
     */
    private String firstText(String first, String second) {
        if (hasText(first)) {
            return first;
        }
        return second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
