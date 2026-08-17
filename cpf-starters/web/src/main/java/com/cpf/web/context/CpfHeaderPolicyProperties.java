package com.cpf.web.context;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/** Dynamic custom-header policy. Unknown headers remain readable without registration. */
@ConfigurationProperties("cpf.web.headers")
/** HTTP Header의 크기·형식·전파·마스킹 정책을 설정으로 관리하는 Configuration Properties입니다. */
public class CpfHeaderPolicyProperties {
    /** 전체 또는 개별 Header의 허용 최대 개수를 제한해 Header 남용을 차단합니다. */
    private int maxCount = 100;
    /** 요청 전체 Header의 허용 최대 바이트를 제한해 과대 요청을 차단합니다. */
    private int maxBytes = 32768;
    /** Header 이름별 required·validation·propagation·masking 정책을 보관합니다. */
    private Map<String, Rule> policies = new LinkedHashMap<>();

    public int getMaxCount() { return maxCount; }
    public void setMaxCount(int maxCount) { this.maxCount = maxCount; }
    public int getMaxBytes() { return maxBytes; }
    public void setMaxBytes(int maxBytes) { this.maxBytes = maxBytes; }
    public Map<String, Rule> getPolicies() { return policies; }
    public void setPolicies(Map<String, Rule> policies) { this.policies = policies == null ? new LinkedHashMap<>() : policies; }

    /** 개별 Header의 수신·검증·전파·마스킹 규칙을 표현하는 설정 단위입니다. */
    public static class Rule {
        /** 해당 Header가 현재 정책 경계에서 필수인지 지정합니다. */
        private boolean required;
        /** 개별 Header 값의 허용 최대 길이를 지정합니다. */
        private int maxLength = 4096;
        /** Header 값에 적용할 정규식 검증 패턴을 지정합니다. */
        private String pattern;
        /** 문자열·숫자·UUID 등 Header typed conversion 기준을 지정합니다. */
        private String type = "string";
        /** 외부 Ingress에서 해당 Header 수신을 허용할지 지정합니다. */
        private boolean inboundAllowed = true;
        /** 내부 Domain 호출로 해당 Header 전파를 허용할지 지정합니다. */
        private boolean internalPropagationAllowed;
        /** 외부기관 호출로 해당 Header 유출을 허용할지 지정합니다. */
        private boolean externalOutboundAllowed;
        /** 해당 Header를 정책상 로그에 기록할 수 있는지 지정합니다. */
        private boolean loggable;
        /** 로그·감사 기록 시 해당 Header 값을 마스킹할지 지정합니다. */
        private boolean masked = true;
        /** 해당 Header를 민감정보로 분류할지 지정합니다. */
        private boolean sensitive;
        /** 전체 또는 개별 Header의 허용 최대 개수를 제한해 Header 남용을 차단합니다. */
        private int maxCount = 1;
        /** 동일 Header의 다중 값 수신을 허용할지 지정합니다. */
        private boolean duplicateAllowed;
        public boolean isRequired() { return required; } public void setRequired(boolean v) { required = v; }
        public int getMaxLength() { return maxLength; } public void setMaxLength(int v) { maxLength = v; }
        public String getPattern() { return pattern; } public void setPattern(String v) { pattern = v; }
        public String getType() { return type; } public void setType(String v) { type = v; }
        public boolean isInboundAllowed() { return inboundAllowed; } public void setInboundAllowed(boolean v) { inboundAllowed = v; }
        public boolean isInternalPropagationAllowed() { return internalPropagationAllowed; } public void setInternalPropagationAllowed(boolean v) { internalPropagationAllowed = v; }
        public boolean isExternalOutboundAllowed() { return externalOutboundAllowed; } public void setExternalOutboundAllowed(boolean v) { externalOutboundAllowed = v; }
        public boolean isLoggable() { return loggable; } public void setLoggable(boolean v) { loggable = v; }
        public boolean isMasked() { return masked; } public void setMasked(boolean v) { masked = v; }
        public boolean isSensitive() { return sensitive; } public void setSensitive(boolean v) { sensitive = v; }
        public int getMaxCount() { return maxCount; } public void setMaxCount(int v) { maxCount = v; }
        public boolean isDuplicateAllowed() { return duplicateAllowed; } public void setDuplicateAllowed(boolean v) { duplicateAllowed = v; }
    }
}
