package fps.cmn.msg.service;

import fps.pfw.common.exception.FpsErrorDefinition;
import fps.pfw.common.exception.FpsMessageResolver;
import fps.pfw.common.exception.FpsResolvedMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * CMN 메시지 캐시를 사용해 PFW 표준 오류 메시지를 해석하는 구현체입니다.
 *
 * <p>PFW는 CMN을 직접 의존하지 않고 {@link FpsMessageResolver} 인터페이스만 알고 있습니다.
 * 이 클래스가 업무 애플리케이션에 Bean으로 등록되면, 표준 예외 핸들러는
 * {@code message_table}에 등록된 고객용/내부용 메시지를 자동으로 사용합니다.</p>
 */
@Primary
@Component
public class CmnFpsMessageResolver implements FpsMessageResolver {
    private static final String EXTERNAL = "EXTERNAL";
    private static final String INTERNAL = "INTERNAL";

    private final MessageCacheService messageCacheService;

    public CmnFpsMessageResolver(MessageCacheService messageCacheService) {
        this.messageCacheService = messageCacheService;
    }

    /**
     * 오류 코드에 매핑된 고객용/내부용 메시지를 조회합니다.
     *
     * @param errorCode 표준 오류 정의
     * @param locale 요청 언어
     * @return 메시지 테이블 값이 있으면 테이블 값을, 없으면 오류 코드 기본 메시지를 반환합니다.
     */
    @Override
    public FpsResolvedMessage resolve(FpsErrorDefinition errorCode, Locale locale) {
        String language = locale == null || locale.getLanguage() == null || locale.getLanguage().isBlank()
                ? "ko"
                : locale.getLanguage();

        String externalMessage = messageValue(
                errorCode.getExternalMessageKey(),
                language,
                EXTERNAL,
                errorCode.getDefaultExternalMessage());
        String internalMessage = messageValue(
                errorCode.getInternalMessageKey(),
                language,
                INTERNAL,
                errorCode.getDefaultInternalMessage());

        return new FpsResolvedMessage(externalMessage, internalMessage);
    }

    private String messageValue(String messageKey, String locale, String messageType, String fallback) {
        Map<String, Object> message = messageCacheService.getMessageByKeyLocaleType(messageKey, locale, messageType);
        if (message == null || message.isEmpty()) {
            return fallback;
        }

        Object value = message.get("messageValue");
        if (value == null) {
            value = message.get("message_value");
        }
        return value == null ? fallback : String.valueOf(value);
    }
}
