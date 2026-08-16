package com.cpf.admin.opr.batch.runtime;

/** BAT Owner 호출의 업무 오류·Unavailable·결과불명을 보존하는 Typed 예외입니다. */
public final class BatchControlClientException extends RuntimeException {
    public enum Category { VALIDATION, CONFLICT, PERMISSION, NOT_FOUND, UNAVAILABLE, UNKNOWN_RESULT, OWNER_ERROR }
    private final Category category;
    private final String errorCode;
    private final String traceId;

    public BatchControlClientException(Category category,String errorCode,String message,String traceId,Throwable cause) {
        super(message,cause);this.category=category;this.errorCode=errorCode;this.traceId=traceId;
    }
    public Category category(){return category;}
    public String errorCode(){return errorCode;}
    public String traceId(){return traceId;}
}
