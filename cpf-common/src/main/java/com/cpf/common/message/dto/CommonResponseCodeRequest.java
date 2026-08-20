package com.cpf.common.message.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/** HTTP status를 소유하지 않는 transport-neutral Response Code catalog 요청입니다. */
public class CommonResponseCodeRequest {
    @NotBlank private String responseCode;
    @NotBlank private String messageCode;
    @NotBlank private String resultType;
    @NotBlank private String moduleId;
    @NotBlank private String responseGroup;
    @NotBlank private String sequenceNo;
    private String category = "BUSINESS";
    private String retryDisposition = "NEVER";
    private String exposure = "SAFE_MESSAGE_ONLY";
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private Long catalogVersion = 1L;
    private String description;
    private String useYn = "Y";
    private String requestUser = "SYSTEM";

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String v) { responseCode=v; }
    public String getMessageCode() { return messageCode; }
    public void setMessageCode(String v) { messageCode=v; }
    public String getResultType() { return resultType; }
    public void setResultType(String v) { resultType=v; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String v) { moduleId=v; }
    public String getResponseGroup() { return responseGroup; }
    public void setResponseGroup(String v) { responseGroup=v; }
    public String getSequenceNo() { return sequenceNo; }
    public void setSequenceNo(String v) { sequenceNo=v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { category=v; }
    public String getRetryDisposition() { return retryDisposition; }
    public void setRetryDisposition(String v) { retryDisposition=v; }
    public String getExposure() { return exposure; }
    public void setExposure(String v) { exposure=v; }
    public Instant getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(Instant v) { effectiveFrom=v; }
    public Instant getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(Instant v) { effectiveTo=v; }
    public Long getCatalogVersion() { return catalogVersion; }
    public void setCatalogVersion(Long v) { catalogVersion=v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description=v; }
    public String getUseYn() { return useYn; }
    public void setUseYn(String v) { useYn=v; }
    public String getRequestUser() { return requestUser; }
    public void setRequestUser(String v) { requestUser=v; }
}
