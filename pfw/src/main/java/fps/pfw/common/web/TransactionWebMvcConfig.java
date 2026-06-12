package fps.pfw.common.web;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class TransactionWebMvcConfig implements WebMvcConfigurer {

    private final TransactionHeaderValidationInterceptor transactionHeaderValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(transactionHeaderValidationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/error",
                        "/favicon.ico",
                        "/webjars/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**");
    }
}
