package com.cpf.integration.http;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.net.http.HttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
/**
 * CPF typed HTTP client의 기본 JDK {@link java.net.http.HttpClient}와 CPF wrapper를 구성합니다.
 *
 * <p>이 구성은 HTTP capability를 명시적으로 선택한 애플리케이션에서만 로딩되며,
 * 연결 시간 제한과 redirect 정책을 {@link CpfHttpClientProperties}의 검증된 값으로 적용합니다.</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(CpfHttpClientProperties.class)
public class CpfHttpClientAutoConfiguration {
 @Bean HttpClient cpfJdkHttpClient(CpfHttpClientProperties p){p.validate();return HttpClient.newBuilder().connectTimeout(p.getConnectTimeout()).followRedirects(HttpClient.Redirect.NEVER).build();}
 @Bean CpfTypedHttpClient cpfTypedHttpClient(HttpClient c,CpfHttpClientProperties p,CpfExecutionIdGenerator executionIds){return new CpfTypedHttpClient(c,p,executionIds);}
}
