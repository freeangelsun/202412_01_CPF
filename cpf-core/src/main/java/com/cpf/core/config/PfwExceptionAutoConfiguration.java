package cpf.pfw.config;

import cpf.pfw.common.exception.CpfGlobalExceptionHandler;
import cpf.pfw.common.exception.CpfMessageResolver;
import cpf.pfw.common.exception.CpfResponseCodeResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * PFW 표준 예외 처리기를 자동 등록하는 설정입니다.
 * 업무 모듈이 별도 예외 처리기를 제공하지 않아도 CPF 공통 오류 응답을 사용할 수 있게 합니다.
 */
@AutoConfiguration
public class PfwExceptionAutoConfiguration {

    /**
     * PFW 공통 예외 처리기를 Bean으로 등록합니다.
     *
     * @param messageResolverProvider 메시지 변환기 Provider
     * @param responseCodeResolverProvider 응답 코드 변환기 Provider
     * @return PFW 공통 예외 처리기
     */
    @Bean
    @ConditionalOnMissingBean
    public CpfGlobalExceptionHandler cpfGlobalExceptionHandler(
            ObjectProvider<CpfMessageResolver> messageResolverProvider,
            ObjectProvider<CpfResponseCodeResolver> responseCodeResolverProvider) {
        return new CpfGlobalExceptionHandler(messageResolverProvider, responseCodeResolverProvider);
    }
}
