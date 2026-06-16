package cpf.cmn.msg.service;

import cpf.pfw.common.exception.FpsErrorDefinition;
import cpf.pfw.common.exception.FpsMessageResolver;
import cpf.pfw.common.exception.FpsResolvedMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * CMN 硫붿떆吏 罹먯떆瑜??ъ슜??PFW ?쒖? ?ㅻ쪟 硫붿떆吏瑜??댁꽍?섎뒗 援ы쁽泥댁엯?덈떎.
 *
 * <p>PFW??CMN??吏곸젒 ?섏〈?섏? ?딄퀬 {@link FpsMessageResolver} ?명꽣?섏씠?ㅻ쭔 ?뚭퀬 ?덉뒿?덈떎.
 * ???대옒?ㅺ? ?낅Т ?좏뵆由ъ??댁뀡??Bean?쇰줈 ?깅줉?섎㈃, ?쒖? ?덉쇅 ?몃뱾?щ뒗
 * {@code message_table}???깅줉??怨좉컼???대???硫붿떆吏瑜??먮룞?쇰줈 ?ъ슜?⑸땲??</p>
 */
@Primary
@Component
public class CmnFpsMessageResolver implements FpsMessageResolver {
    private final MessageCacheService messageCacheService;

    public CmnFpsMessageResolver(MessageCacheService messageCacheService) {
        this.messageCacheService = messageCacheService;
    }

    /**
     * ?ㅻ쪟 肄붾뱶??留ㅽ븨??怨좉컼???대???硫붿떆吏瑜?議고쉶?⑸땲??
     *
     * @param errorCode ?쒖? ?ㅻ쪟 ?뺤쓽
     * @param locale ?붿껌 ?몄뼱
     * @return 硫붿떆吏 ?뚯씠釉?媛믪씠 ?덉쑝硫??뚯씠釉?媛믪쓣, ?놁쑝硫??ㅻ쪟 肄붾뱶 湲곕낯 硫붿떆吏瑜?諛섑솚?⑸땲??
     */
    @Override
    public FpsResolvedMessage resolve(FpsErrorDefinition errorCode, Locale locale) {
        String language = locale == null || locale.getLanguage() == null || locale.getLanguage().isBlank()
                ? "ko"
                : locale.getLanguage();

        Map<String, Object> message = messageCacheService.getMessageByKeyAndLocale(errorCode.getMessageCode(), language);
        String externalMessage = mapValue(message, "externalMessage", "external_message", errorCode.getDefaultExternalMessage());
        String internalMessage = mapValue(message, "internalMessage", "internal_message", errorCode.getDefaultInternalMessage());

        return new FpsResolvedMessage(externalMessage, internalMessage);
    }

    private String mapValue(Map<String, Object> message, String camelKey, String snakeKey, String fallback) {
        if (message == null || message.isEmpty()) {
            return fallback;
        }

        Object value = message.get(camelKey);
        if (value == null) {
            value = message.get(snakeKey);
        }
        if (value == null && snakeKey != null) {
            value = message.get(snakeKey.toUpperCase());
        }
        return value == null ? fallback : String.valueOf(value);
    }
}

