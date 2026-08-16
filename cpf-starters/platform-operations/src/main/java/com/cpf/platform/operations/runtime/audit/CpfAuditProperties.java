package com.cpf.platform.operations.runtime.audit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.platform-operations.audit")
/** CpfAuditProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfAuditProperties {
    private boolean enabled = true;
    private boolean failClosed = true;
    private int summaryMaxLength = 256;
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public boolean isFailClosed(){return failClosed;} public void setFailClosed(boolean v){failClosed=v;}
    public int getSummaryMaxLength(){return summaryMaxLength;} public void setSummaryMaxLength(int v){if(v<16||v>4096)throw new IllegalArgumentException("summaryMaxLength");summaryMaxLength=v;}
}
