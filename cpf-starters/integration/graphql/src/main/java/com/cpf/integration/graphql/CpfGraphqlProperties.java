package com.cpf.integration.graphql;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** GraphQL Optional Capability의 fail-closed 안전 한계. */
@ConfigurationProperties("cpf.integration.graphql")
public class CpfGraphqlProperties {
    private boolean enabled;
    private int maxDepth = 12;
    private int maxComplexity = 250;
    private int maxDocumentLength = 32768;
    private int maxRequestBytes = 131072;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private int maxRequestsPerSecond = 50;
    private Duration timeout = Duration.ofSeconds(5);
    private boolean requireAuthenticated = true;
    private boolean requireTenant = true;
    private boolean introspection;
    private boolean graphiql;

    public boolean isEnabled(){ return enabled; } public void setEnabled(boolean v){ enabled=v; }
    public int getMaxDepth(){ return maxDepth; } public void setMaxDepth(int v){ maxDepth=requirePositive(v,"maxDepth"); }
    public int getMaxComplexity(){ return maxComplexity; } public void setMaxComplexity(int v){ maxComplexity=requirePositive(v,"maxComplexity"); }
    public int getMaxDocumentLength(){ return maxDocumentLength; } public void setMaxDocumentLength(int v){ if(v<1024)throw new IllegalArgumentException("maxDocumentLength");maxDocumentLength=v; }
    public int getMaxRequestBytes(){ return maxRequestBytes; } public void setMaxRequestBytes(int v){ if(v<4096)throw new IllegalArgumentException("maxRequestBytes");maxRequestBytes=v; }
    public int getMaxRequestsPerSecond(){ return maxRequestsPerSecond; } public void setMaxRequestsPerSecond(int v){ maxRequestsPerSecond=requirePositive(v,"maxRequestsPerSecond"); }
    public Duration getTimeout(){ return timeout; } public void setTimeout(Duration v){ if(v==null||v.isZero()||v.isNegative()||v.compareTo(Duration.ofMinutes(1))>0)throw new IllegalArgumentException("timeout");timeout=v; }
    public boolean isRequireAuthenticated(){ return requireAuthenticated; } public void setRequireAuthenticated(boolean v){ requireAuthenticated=v; }
    public boolean isRequireTenant(){ return requireTenant; } public void setRequireTenant(boolean v){ requireTenant=v; }
    public boolean isIntrospection(){ return introspection; } public void setIntrospection(boolean v){ introspection=v; }
    public boolean isGraphiql(){ return graphiql; } public void setGraphiql(boolean v){ graphiql=v; }
    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
    public void validate(){ requirePositive(maxDepth,"maxDepth");requirePositive(maxComplexity,"maxComplexity");requirePositive(maxRequestsPerSecond,"maxRequestsPerSecond");setTimeout(timeout); }
    private static int requirePositive(int value,String name){if(value<1)throw new IllegalArgumentException(name);return value;}
}
