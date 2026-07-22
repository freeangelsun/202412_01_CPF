package com.cpf.core.common.logging;

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
}
