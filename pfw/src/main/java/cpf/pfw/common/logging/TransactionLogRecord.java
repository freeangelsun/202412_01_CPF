package cpf.pfw.common.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PFW 嫄곕옒 濡쒓렇 ??嫄댁쓣 ?쒗쁽?섎뒗 ?곗씠??媛앹껜?낅땲??
 *
 * <p>{@code LoggingAspect}媛 而⑦듃濡ㅻ윭 ?ㅽ뻾 ?뺣낫瑜???媛앹껜???닿퀬,
 * {@code TransactionLogService}媛 TRAN_LOG ?뚯씠釉붿뿉 ??ν빀?덈떎.
 * 蹂몃Ц, ?뚮씪誘명꽣, ?묐떟泥섎읆 ???곗씠?곕뒗 ??媛앹껜?먮룄 ?붿빟 ??ν븯怨? * TRAN_LOG_DTL?먮뒗 ??媛??곸꽭 濡쒓렇濡???踰?????ν빀?덈떎.</p>
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

