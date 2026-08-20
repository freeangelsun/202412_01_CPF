package com.cpf.common.message.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/** CPF Common Message의 locale/version/effective/parameter 안전정책을 포함한 관리 요청입니다. */
public class CommonMessageRequest {
    private Long messageId;
    private String messageCode;
    private String messageKey;
    @NotBlank private String locale;
    private String messageFormatType = "FIXED";
    private String externalMessage;
    private String internalMessage;
    private String messageValue;
    private Integer parameterCount = 0;
    private String parameterSample;
    private String parameterSchemaJson;
    private String escapeHtmlYn = "Y";
    private String maskArgumentsYn = "Y";
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private Long catalogVersion = 1L;
    private String description;
    private String useYn = "Y";
    private String requestUser = "SYSTEM";
    private String reason;

    public String getEffectiveMessageCode() { return hasText(messageCode) ? messageCode : messageKey; }
    public String getEffectiveExternalMessage() { return hasText(externalMessage) ? externalMessage : messageValue; }
    public String getEffectiveInternalMessage() { return hasText(internalMessage) ? internalMessage : getEffectiveExternalMessage(); }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }

    public Long getMessageId(){return messageId;} public void setMessageId(Long v){messageId=v;}
    public String getMessageCode(){return messageCode;} public void setMessageCode(String v){messageCode=v;}
    public String getMessageKey(){return messageKey;} public void setMessageKey(String v){messageKey=v;}
    public String getLocale(){return locale;} public void setLocale(String v){locale=v;}
    public String getMessageFormatType(){return messageFormatType;} public void setMessageFormatType(String v){messageFormatType=v;}
    public String getExternalMessage(){return externalMessage;} public void setExternalMessage(String v){externalMessage=v;}
    public String getInternalMessage(){return internalMessage;} public void setInternalMessage(String v){internalMessage=v;}
    public String getMessageValue(){return messageValue;} public void setMessageValue(String v){messageValue=v;}
    public Integer getParameterCount(){return parameterCount;} public void setParameterCount(Integer v){parameterCount=v;}
    public String getParameterSample(){return parameterSample;} public void setParameterSample(String v){parameterSample=v;}
    public String getParameterSchemaJson(){return parameterSchemaJson;} public void setParameterSchemaJson(String v){parameterSchemaJson=v;}
    public String getEscapeHtmlYn(){return escapeHtmlYn;} public void setEscapeHtmlYn(String v){escapeHtmlYn=v;}
    public String getMaskArgumentsYn(){return maskArgumentsYn;} public void setMaskArgumentsYn(String v){maskArgumentsYn=v;}
    public Instant getEffectiveFrom(){return effectiveFrom;} public void setEffectiveFrom(Instant v){effectiveFrom=v;}
    public Instant getEffectiveTo(){return effectiveTo;} public void setEffectiveTo(Instant v){effectiveTo=v;}
    public Long getCatalogVersion(){return catalogVersion;} public void setCatalogVersion(Long v){catalogVersion=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getUseYn(){return useYn;} public void setUseYn(String v){useYn=v;}
    public String getRequestUser(){return requestUser;} public void setRequestUser(String v){requestUser=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
}
