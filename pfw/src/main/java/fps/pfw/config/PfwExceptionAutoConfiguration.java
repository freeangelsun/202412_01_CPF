package fps.pfw.config;

import fps.pfw.common.exception.FpsGlobalExceptionHandler;
import fps.pfw.common.exception.FpsMessageResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * PFW 표준 예외 핸들러 자동 설정입니다.
 *
 * <p>업무 애플리케이션이 {@code fps.pfw} 패키지를 직접 컴포넌트 스캔하지 않아도
 * PFW 표준 오류 응답을 사용할 수 있게 합니다.</p>
 */
@AutoConfiguration
public class PfwExceptionAutoConfiguration {

    /**
     * PFW 표준 예외 핸들러를 Bean으로 등록합니다.
     *
     * @param messageResolverProvider CMN 또는 업무 모듈이 제공하는 메시지 리졸버
     * @return 표준 예외 핸들러
     */
    @Bean
    @ConditionalOnMissingBean
    public FpsGlobalExceptionHandler fpsGlobalExceptionHandler(ObjectProvider<FpsMessageResolver> messageResolverProvider) {
        return new FpsGlobalExceptionHandler(messageResolverProvider);
    }
}
