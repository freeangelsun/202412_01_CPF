package cpf.pfw.common.exception;

import java.util.Locale;

/**
 * 蹂꾨룄 硫붿떆吏 ??μ냼媛 ?놁쓣 ???ъ슜?섎뒗 湲곕낯 硫붿떆吏 由ъ「踰꾩엯?덈떎.
 */
public class DefaultFpsMessageResolver implements FpsMessageResolver {

    @Override
    public FpsResolvedMessage resolve(FpsErrorDefinition errorCode, Locale locale) {
        return new FpsResolvedMessage(
                errorCode.getDefaultExternalMessage(),
                errorCode.getDefaultInternalMessage());
    }
}

