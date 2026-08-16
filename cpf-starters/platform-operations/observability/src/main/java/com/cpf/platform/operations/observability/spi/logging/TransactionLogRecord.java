package com.cpf.platform.operations.observability.spi.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CPF 거래 로그 요약 저장 객체입니다.
 *
 * <p>{@code LoggingAspect}가 요청, 응답, 실행 메타, 표준 거래 헤더를 수집하고
 * {@code TransactionLogService}가 {@code cpf_transaction_log}에 저장합니다.
 * 본문처럼 큰 데이터는 요약 로그와 상세 로그에 나누어 저장합니다.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLogRecord {
    private Long logIdx;
    private String recoveryEventId;
    private String transactionId;
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private Integer sequenceNo;
    private String moduleId;
    private String menuId;
    private String businessTransactionId;
    private String businessTransactionName;
    private String logType;
    private String apiVersion;
    private String clientAppId;
    private String clientVersion;
    private String callerService;
    private String callerInstanceId;
    private String correlationId;
    private String idempotencyKey;
    private String locale;
    private String timezone;
    private String requestType;
    private String originalChannelCode;
    private String channelCode;
    private String memberNo;
    private String customerNo;
    private String screenId;
    private String deviceId;
    private String clientRequestTime;
    private String wasId;
    private String serverInstanceId;
    private String hostName;
    private String processId;
    private String threadName;
    private String reservedField1;
    private String reservedField2;
    private String reservedField3;
    private String reservedField4;
    private String reservedField5;
    private String httpMethod;
    private String uri;
    private String controller;
    private String executionPackage;
    private String executionClass;
    private String executionMethod;
    private String executionSignature;
    private String workflowId;
    private String workflowName;
    private String workflowInstanceId;
    private String workflowStepId;
    private String workflowStepName;
    private String workflowStatus;
    private String workflowFailurePolicy;
    private String compensationYn;
    private String compensationTransactionId;
    private String compensationTargetTransactionId;
    private String compensationStatus;
    private String parameters;
    private String requestBody;
    private String response;
    private Integer httpStatus;
    private String responseCode;
    private String messageCode;
    private String messageContent;
    private String errorMessage;
    private String errorCode;
    private String externalMessage;
    private String internalMessage;
    private String execUser;
    private String clientIp;
    private String userAgent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;

    /**
     * 표준 실행 정의 ID(O/S/B + SystemCode 기반)를 반환합니다.
     * 기존 businessTransactionId 필드는 저장 호환성을 위해 남겨 두되 신규 코드는 이 명칭을 사용합니다.
     */
    public String getStandardExecutionId() {
        return businessTransactionId;
    }

    public void setStandardExecutionId(String standardExecutionId) {
        this.businessTransactionId = standardExecutionId;
    }

    public String getStandardExecutionName() {
        return businessTransactionName;
    }

    public void setStandardExecutionName(String standardExecutionName) {
        this.businessTransactionName = standardExecutionName;
    }

    /**
     * 신규 Source/EDU는 공식 standardExecution 명칭을 사용하고,
     * 내부 저장 필드는 기존 businessTransaction* 필드에 매핑해 호환성을 유지합니다.
     */
    public static class TransactionLogRecordBuilder {
        public TransactionLogRecordBuilder standardExecutionId(String standardExecutionId) {
            this.businessTransactionId = standardExecutionId;
            return this;
        }

        /** standardExecutionName는 거래 로그 lineage에 필요한 표준 실행·사용자 문맥을 일관되게 제공합니다. */
        public TransactionLogRecordBuilder standardExecutionName(String standardExecutionName) {
            this.businessTransactionName = standardExecutionName;
            return this;
        }
    }
}
