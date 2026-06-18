package cpf.pfw.common.exception;

import java.util.Locale;

/**
 * 蹂꾨룄 硫붿떆吏 ??μ냼媛 ?놁쓣 ???ъ슜?섎뒗 湲곕낯 硫붿떆吏 由ъ「踰꾩엯?덈떎.
 */
public class DefaultCpfMessageResolver implements CpfMessageResolver {

    @Override
    public CpfResolvedMessage resolve(CpfErrorDefinition errorCode, Locale locale) {
        return new CpfResolvedMessage(
                errorCode.getDefaultExternalMessage(),
                errorCode.getDefaultInternalMessage());
    }
}

