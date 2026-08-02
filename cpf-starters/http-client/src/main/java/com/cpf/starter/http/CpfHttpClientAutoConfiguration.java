package com.cpf.starter.http;
import java.net.http.HttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
@AutoConfiguration
@EnableConfigurationProperties(CpfHttpClientProperties.class)
public class CpfHttpClientAutoConfiguration {
 @Bean HttpClient cpfJdkHttpClient(CpfHttpClientProperties p){p.validate();return HttpClient.newBuilder().connectTimeout(p.getConnectTimeout()).followRedirects(HttpClient.Redirect.NEVER).build();}
 @Bean CpfTypedHttpClient cpfTypedHttpClient(HttpClient c,CpfHttpClientProperties p){return new CpfTypedHttpClient(c,p);}
}
