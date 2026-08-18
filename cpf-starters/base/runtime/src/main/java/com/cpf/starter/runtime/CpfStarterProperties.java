package com.cpf.starter.runtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 모든 CPF Application이 공통으로 사용하는 경량 Base Starter 설정입니다. */
@ConfigurationProperties("cpf.starter")
public class CpfStarterProperties {
    private boolean strict = true;
    private boolean diagnostics = true;
    private boolean loggingAnnotationEnabled = true;
    private boolean performanceAnnotationEnabled = true;
    private int logValueMaxLength = 256;
    /** 성능 경고 기준 시간이며 0 이하 입력은 안전한 최소값으로 보정합니다. */
    private long performanceSlowThresholdMillis = 1000L;

    public boolean isStrict() { return strict; }
    public void setStrict(boolean strict) { this.strict = strict; }
    public boolean isDiagnostics() { return diagnostics; }
    public void setDiagnostics(boolean diagnostics) { this.diagnostics = diagnostics; }
    public boolean isLoggingAnnotationEnabled() { return loggingAnnotationEnabled; }
    public void setLoggingAnnotationEnabled(boolean value) { this.loggingAnnotationEnabled = value; }
    public boolean isPerformanceAnnotationEnabled() { return performanceAnnotationEnabled; }
    public void setPerformanceAnnotationEnabled(boolean value) { this.performanceAnnotationEnabled = value; }
    public long getPerformanceSlowThresholdMillis() { return performanceSlowThresholdMillis; }
    public void setPerformanceSlowThresholdMillis(long value) { this.performanceSlowThresholdMillis = Math.max(1L, value); }
    public int getLogValueMaxLength() { return logValueMaxLength; }
    public void setLogValueMaxLength(int value) { this.logValueMaxLength = Math.max(32, Math.min(4096, value)); }
}
