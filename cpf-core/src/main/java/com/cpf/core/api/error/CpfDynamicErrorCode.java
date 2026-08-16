package com.cpf.core.api.error;

import java.util.Objects;

/** Enum 추가 없이 업무별 메시지 키와 기술중립 오류 의미를 조합하는 공개 오류 정의입니다. */
public final class CpfDynamicErrorCode implements CpfErrorDefinition {
    private final String statusCode; private final String messageCode; private final String messageKeyPrefix;
    private final Category category; private final RetryDisposition retryDisposition; private final Exposure exposure;
    private final String externalMessage; private final String internalMessage;

    public CpfDynamicErrorCode(String statusCode,String messageCode,String messageKeyPrefix,Category category,
                               RetryDisposition retryDisposition,Exposure exposure,String externalMessage,String internalMessage){
        this.statusCode=require(statusCode,"statusCode"); this.messageCode=require(messageCode,"messageCode");
        this.messageKeyPrefix=require(messageKeyPrefix,"messageKeyPrefix"); this.category=Objects.requireNonNull(category,"category");
        this.retryDisposition=retryDisposition==null?RetryDisposition.NEVER:retryDisposition;
        this.exposure=exposure==null?defaultExposure(category):exposure;
        this.externalMessage=require(externalMessage,"externalMessage"); this.internalMessage=require(internalMessage,"internalMessage");
    }
    /** business는 동적 오류 정의를 표준 분류로 변환하면서 오류 코드와 재시도 의미를 보존합니다. */
    public static CpfDynamicErrorCode business(String key,String external,String internal){return from(CpfErrorCode.BUSINESS_RULE_VIOLATION,key,external,internal);}
    /** duplicate는 동적 오류 정의를 표준 분류로 변환하면서 오류 코드와 재시도 의미를 보존합니다. */
    public static CpfDynamicErrorCode duplicate(String key,String external,String internal){return from(CpfErrorCode.DUPLICATE,key,external,internal);}
    /** from는 동적 오류 정의를 표준 분류로 변환하면서 오류 코드와 재시도 의미를 보존합니다. */
    public static CpfDynamicErrorCode from(CpfErrorDefinition base,String key,String external,String internal){
        Objects.requireNonNull(base,"base"); return new CpfDynamicErrorCode(base.statusCode(),base.messageCode(),key,base.category(),base.retryDisposition(),base.exposure(),external,internal);
    }
    @Override public String statusCode(){return statusCode;} @Override public String messageCode(){return messageCode;}
    @Override public Category category(){return category;} @Override public RetryDisposition retryDisposition(){return retryDisposition;}
    @Override public Exposure exposure(){return exposure;} @Override public String defaultExternalMessage(){return externalMessage;}
    @Override public String defaultInternalMessage(){return internalMessage;}
    @Override public String getExternalMessageKey(){return messageKeyPrefix;} @Override public String getInternalMessageKey(){return messageKeyPrefix;}
    private static Exposure defaultExposure(Category c){return switch(c){case VALIDATION,NOT_FOUND,CONFLICT,RATE_LIMIT,AUTHENTICATION,AUTHORIZATION,BUSINESS->Exposure.SAFE_MESSAGE_ONLY;case EXTERNAL,INFRASTRUCTURE,INTERNAL->Exposure.GENERIC_MESSAGE_ONLY;};}
    private static String require(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
}
