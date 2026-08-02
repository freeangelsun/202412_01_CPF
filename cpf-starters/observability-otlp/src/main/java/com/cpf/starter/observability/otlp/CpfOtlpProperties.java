package com.cpf.starter.observability.otlp;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("cpf.observability.otlp")
public class CpfOtlpProperties {private boolean enabled;private String endpoint;private Duration timeout=Duration.ofSeconds(10);private double sampleProbability=1.0;
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}public String getEndpoint(){return endpoint;}public void setEndpoint(String v){endpoint=v;}public Duration getTimeout(){return timeout;}public void setTimeout(Duration v){timeout=v;}public double getSampleProbability(){return sampleProbability;}public void setSampleProbability(double v){sampleProbability=v;}
 public void validate(){if(!enabled)return;if(endpoint==null||endpoint.isBlank())throw new IllegalStateException("OTLP endpoint is required");if(sampleProbability<0||sampleProbability>1)throw new IllegalStateException("sample-probability must be 0..1");}}
