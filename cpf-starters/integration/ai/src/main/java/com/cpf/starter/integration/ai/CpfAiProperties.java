package com.cpf.starter.integration.ai;
import com.cpf.core.api.config.CpfConfigMutability;
import com.cpf.core.api.config.CpfConfigPolicy;
import java.time.Duration;import java.util.*;import org.springframework.boot.context.properties.ConfigurationProperties;
@CpfConfigPolicy(prefix="cpf.integration.ai", mutability=CpfConfigMutability.RESTART_REQUIRED, secretSeparated=false)
@ConfigurationProperties("cpf.integration.ai")
public class CpfAiProperties {
 private boolean enabled;private List<String> providerOrder=new ArrayList<>();private int maxAttempts=3;private int retryAttemptsPerProvider=2;private Duration timeout=Duration.ofSeconds(10);private Duration retryBackoff=Duration.ofMillis(100);private int circuitFailureThreshold=3;private Duration circuitOpenDuration=Duration.ofSeconds(30);
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public List<String> getProviderOrder(){return providerOrder;}public void setProviderOrder(List<String> v){providerOrder=v==null?new ArrayList<>():new ArrayList<>(v);}
 public int getMaxAttempts(){return maxAttempts;}public void setMaxAttempts(int v){if(v<1||v>10)throw new IllegalArgumentException("max-attempts must be 1..10");maxAttempts=v;}
 public int getRetryAttemptsPerProvider(){return retryAttemptsPerProvider;}public void setRetryAttemptsPerProvider(int v){if(v<1||v>5)throw new IllegalArgumentException("retry-attempts-per-provider must be 1..5");retryAttemptsPerProvider=v;}
 public Duration getTimeout(){return timeout;}public void setTimeout(Duration v){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException("timeout positive");timeout=v;}
 public Duration getRetryBackoff(){return retryBackoff;}public void setRetryBackoff(Duration v){if(v==null||v.isNegative())throw new IllegalArgumentException("retry-backoff must not be negative");retryBackoff=v;}
 public int getCircuitFailureThreshold(){return circuitFailureThreshold;}public void setCircuitFailureThreshold(int v){if(v<1||v>100)throw new IllegalArgumentException("circuit-failure-threshold must be 1..100");circuitFailureThreshold=v;}
 public Duration getCircuitOpenDuration(){return circuitOpenDuration;}public void setCircuitOpenDuration(Duration v){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException("circuit-open-duration positive");circuitOpenDuration=v;}
}
