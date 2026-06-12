package fps.pfw.common.exception;

import java.util.Locale;

/**
 * 별도 메시지 저장소가 없을 때 사용하는 기본 메시지 리졸버입니다.
 */
public class DefaultFpsMessageResolver implements FpsMessageResolver {

    @Override
    public FpsResolvedMessage resolve(FpsErrorDefinition errorCode, Locale locale) {
        return new FpsResolvedMessage(
                errorCode.getDefaultExternalMessage(),
                errorCode.getDefaultInternalMessage());
    }
}
