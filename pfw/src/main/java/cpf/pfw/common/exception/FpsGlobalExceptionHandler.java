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

import java.util.Locale;

/**
 * FPS ?쒖? ?덉쇅瑜?怨듯넻 ?ㅻ쪟 ?묐떟?쇰줈 蹂?섑븯???꾨젅?꾩썙???몃뱾?ъ엯?덈떎.
 *
 * <p>?낅Т 媛쒕컻?먮뒗 而⑦듃濡ㅻ윭?먯꽌 try/catch瑜?諛섎났?섏? ?딄퀬
 * {@link FpsException} 怨꾩뿴 ?덉쇅瑜??섏?硫??⑸땲?? ???몃뱾?ш? ?ㅻ쪟肄붾뱶,
 * 怨좉컼??硫붿떆吏, 嫄곕옒ID/TraceId, ?묐떟 ?ㅻ뜑瑜??쒖? ?뺤떇?쇰줈 ?대젮蹂대깄?덈떎.</p>
 */
@Order(-100)
@RestControllerAdvice
public class FpsGlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(FpsGlobalExceptionHandler.class);

    private final FpsMessageResolver messageResolver;
    private final FpsResponseCodeResolver responseCodeResolver;

    public FpsGlobalExceptionHandler(
            ObjectProvider<FpsMessageResolver> messageResolverProvider,
            ObjectProvider<FpsResponseCodeResolver> responseCodeResolverProvider) {
        this.messageResolver = messageResolverProvider.getIfAvailable(DefaultFpsMessageResolver::new);
        this.responseCodeResolver = responseCodeResolverProvider.getIfAvailable(DefaultFpsResponseCodeResolver::new);
    }

    /**
     * FPS ?쒖? ?덉쇅瑜?泥섎━?⑸땲??
     *
     * @param ex      ?낅Т/?꾨젅?꾩썙?ъ뿉???섏쭊 ?쒖? ?덉쇅
     * @param request ?꾩옱 HTTP ?붿껌
     * @return 怨좉컼?먭쾶 諛섑솚???쒖? ?ㅻ쪟 ?묐떟
     */
    @ExceptionHandler(FpsException.class)
    public ResponseEntity<FpsErrorResponse> handleFpsException(FpsException ex, HttpServletRequest request) {
        FpsErrorDefinition errorCode = ex.getErrorCode();
        FpsResolvedResponse resolvedResponse = errorCode != null
                ? responseCodeResolver.resolve(errorCode, request.getLocale(), ex.getMessageArguments(), ex.getDetail())
                : responseCodeResolver.resolve(ex.getResponseCode(), request.getLocale(), ex.getMessageArguments(), ex.getDetail());
        String externalMessage = firstText(ex.getExternalMessage(), resolvedResponse.externalMessage());
        String internalMessage = firstText(ex.getInternalMessage(), resolvedResponse.internalMessage());

        log.warn(
                "FPS exception handled. statusCode={}, messageCode={}, externalMessage={}, internalMessage={}, detail={}, uri={}",
                resolvedResponse.responseCode(),
                resolvedResponse.messageCode(),
                externalMessage,
                SensitiveDataMasker.mask(internalMessage),
                SensitiveDataMasker.mask(ex.getDetail()),
                request.getRequestURI(),
                ex);

        FpsErrorResponse response = FpsErrorResponse.of(
                resolvedResponse,
                externalMessage,
                ex.getClass().getSimpleName(),
                null);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Code", resolvedResponse.errorCode());
        headers.add("X-Message-Code", resolvedResponse.messageCode());
        headers.add("X-Fps-Response-Code", resolvedResponse.responseCode());
        headers.add("X-Fps-Response-Message-Code", resolvedResponse.messageCode());
        headers.add("X-Fps-Message-Code", resolvedResponse.messageCode());
        headers.add("X-Error-Type", ex.getClass().getSimpleName());

        return ResponseEntity.status(resolvedResponse.httpStatus())
                .headers(headers)
                .body(response);
    }

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

