package cpf.pfw.config;

import cpf.pfw.common.exception.FpsGlobalExceptionHandler;
import cpf.pfw.common.exception.FpsMessageResolver;
import cpf.pfw.common.exception.FpsResponseCodeResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * PFW ?쒖? ?덉쇅 ?몃뱾???먮룞 ?ㅼ젙?낅땲??
 *
 * <p>?낅Т ?좏뵆由ъ??댁뀡??{@code cpf.pfw} ?⑦궎吏瑜?吏곸젒 而댄룷?뚰듃 ?ㅼ틪?섏? ?딆븘?? * PFW ?쒖? ?ㅻ쪟 ?묐떟???ъ슜?????덇쾶 ?⑸땲??</p>
 */
@AutoConfiguration
public class PfwExceptionAutoConfiguration {

    /**
     * PFW ?쒖? ?덉쇅 ?몃뱾?щ? Bean?쇰줈 ?깅줉?⑸땲??
     *
     * @param messageResolverProvider CMN ?먮뒗 ?낅Т ⑤뱢???쒓났?섎뒗 硫붿떆吏 由ъ「踰?     * @return ?쒖? ?덉쇅 ?몃뱾??     */
    @Bean
    @ConditionalOnMissingBean
    public FpsGlobalExceptionHandler fpsGlobalExceptionHandler(
            ObjectProvider<FpsMessageResolver> messageResolverProvider,
            ObjectProvider<FpsResponseCodeResolver> responseCodeResolverProvider) {
        return new FpsGlobalExceptionHandler(messageResolverProvider, responseCodeResolverProvider);
    }
}

