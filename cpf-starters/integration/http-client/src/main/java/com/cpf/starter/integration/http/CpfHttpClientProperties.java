package com.cpf.starter.integration.http;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("cpf.http-client")
public class CpfHttpClientProperties {
 private Duration connectTimeout=Duration.ofSeconds(3); private Duration requestTimeout=Duration.ofSeconds(10); private int maxResponseBytes=4*1024*1024;
 public Duration getConnectTimeout(){return connectTimeout;} public void setConnectTimeout(Duration v){connectTimeout=v;}
 public Duration getRequestTimeout(){return requestTimeout;} public void setRequestTimeout(Duration v){requestTimeout=v;}
 public int getMaxResponseBytes(){return maxResponseBytes;} public void setMaxResponseBytes(int v){maxResponseBytes=v;}
 public void validate(){if(connectTimeout==null||connectTimeout.isNegative()||connectTimeout.isZero())throw new IllegalStateException("connect-timeout must be positive");if(requestTimeout==null||requestTimeout.isNegative()||requestTimeout.isZero())throw new IllegalStateException("request-timeout must be positive");if(maxResponseBytes<1024)throw new IllegalStateException("max-response-bytes must be >=1024");}
}
