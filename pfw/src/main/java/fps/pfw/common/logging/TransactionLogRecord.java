package fps.pfw.common.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PFW 거래 로그 한 건을 표현하는 데이터 객체입니다.
 *
 * <p>{@code LoggingAspect}가 컨트롤러 실행 정보를 이 객체에 담고,
 * {@code TransactionLogService}가 TRAN_LOG 테이블에 저장합니다.
 * 본문, 파라미터, 응답처럼 큰 데이터는 이 객체에도 요약 저장하고
 * TRAN_LOG_DTL에는 키-값 상세 로그로 한 번 더 저장합니다.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLogRecord {
    private Long logIdx;
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
    private String requestType;
    private String originalChannelCode;
    private String channelCode;
    private String memberNo;
    private String customerNo;
    private String screenId;
    private String deviceId;
    private String clientRequestTime;
    private String wasId;
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
    private Integer responseCode;
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
