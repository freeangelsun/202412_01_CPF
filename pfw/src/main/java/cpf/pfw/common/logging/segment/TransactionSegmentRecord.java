package cpf.pfw.common.logging.segment;

import java.time.LocalDateTime;

/**
 * {@code pfw_transaction_segment}에 저장하는 구간 실행 기록입니다.
 */
public class TransactionSegmentRecord {
    private String transactionSegmentId;
    private String transactionGlobalId;
    private String rootTransactionGlobalId;
    private String parentTransactionGlobalId;
    private String parentSegmentId;
    private String transactionRole;
    private String moduleCode;
    private String sourceModuleCode;
    private String targetModuleCode;
    private String direction;
    private int callDepth;
    private int sequenceNo;
    private String apiPath;
    private String transactionName;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long durationMs;
    private String status;
    private String failureYn;
    private String failureCode;
    private String failureMessageMasked;
    private String requestHeaderSnapshotMasked;
    private String responseHeaderSnapshotMasked;
    private String extensionHeaderSnapshotMasked;
    private String customerNoMasked;
    private String memberNoMasked;
    private String userIdMasked;
    private String operatorIdMasked;
    private String channelCode;
    private String originalChannelCode;
    private String clientAppId;
    private String callerService;
    private String externalInstitutionCode;
    private String externalTransactionId;
    private String createdBy;
    private String updatedBy;

    public String getTransactionSegmentId() {
        return transactionSegmentId;
    }

    public void setTransactionSegmentId(String transactionSegmentId) {
        this.transactionSegmentId = transactionSegmentId;
    }

    public String getTransactionGlobalId() {
        return transactionGlobalId;
    }

    public void setTransactionGlobalId(String transactionGlobalId) {
        this.transactionGlobalId = transactionGlobalId;
    }

    public String getRootTransactionGlobalId() {
        return rootTransactionGlobalId;
    }

    public void setRootTransactionGlobalId(String rootTransactionGlobalId) {
        this.rootTransactionGlobalId = rootTransactionGlobalId;
    }

    public String getParentTransactionGlobalId() {
        return parentTransactionGlobalId;
    }

    public void setParentTransactionGlobalId(String parentTransactionGlobalId) {
        this.parentTransactionGlobalId = parentTransactionGlobalId;
    }

    public String getParentSegmentId() {
        return parentSegmentId;
    }

    public void setParentSegmentId(String parentSegmentId) {
        this.parentSegmentId = parentSegmentId;
    }

    public String getTransactionRole() {
        return transactionRole;
    }

    public void setTransactionRole(String transactionRole) {
        this.transactionRole = transactionRole;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getSourceModuleCode() {
        return sourceModuleCode;
    }

    public void setSourceModuleCode(String sourceModuleCode) {
        this.sourceModuleCode = sourceModuleCode;
    }

    public String getTargetModuleCode() {
        return targetModuleCode;
    }

    public void setTargetModuleCode(String targetModuleCode) {
        this.targetModuleCode = targetModuleCode;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getCallDepth() {
        return callDepth;
    }

    public void setCallDepth(int callDepth) {
        this.callDepth = callDepth;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(int sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureYn() {
        return failureYn;
    }

    public void setFailureYn(String failureYn) {
        this.failureYn = failureYn;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public void setFailureCode(String failureCode) {
        this.failureCode = failureCode;
    }

    public String getFailureMessageMasked() {
        return failureMessageMasked;
    }

    public void setFailureMessageMasked(String failureMessageMasked) {
        this.failureMessageMasked = failureMessageMasked;
    }

    public String getRequestHeaderSnapshotMasked() {
        return requestHeaderSnapshotMasked;
    }

    public void setRequestHeaderSnapshotMasked(String requestHeaderSnapshotMasked) {
        this.requestHeaderSnapshotMasked = requestHeaderSnapshotMasked;
    }

    public String getResponseHeaderSnapshotMasked() {
        return responseHeaderSnapshotMasked;
    }

    public void setResponseHeaderSnapshotMasked(String responseHeaderSnapshotMasked) {
        this.responseHeaderSnapshotMasked = responseHeaderSnapshotMasked;
    }

    public String getExtensionHeaderSnapshotMasked() {
        return extensionHeaderSnapshotMasked;
    }

    public void setExtensionHeaderSnapshotMasked(String extensionHeaderSnapshotMasked) {
        this.extensionHeaderSnapshotMasked = extensionHeaderSnapshotMasked;
    }

    public String getCustomerNoMasked() {
        return customerNoMasked;
    }

    public void setCustomerNoMasked(String customerNoMasked) {
        this.customerNoMasked = customerNoMasked;
    }

    public String getMemberNoMasked() {
        return memberNoMasked;
    }

    public void setMemberNoMasked(String memberNoMasked) {
        this.memberNoMasked = memberNoMasked;
    }

    public String getUserIdMasked() {
        return userIdMasked;
    }

    public void setUserIdMasked(String userIdMasked) {
        this.userIdMasked = userIdMasked;
    }

    public String getOperatorIdMasked() {
        return operatorIdMasked;
    }

    public void setOperatorIdMasked(String operatorIdMasked) {
        this.operatorIdMasked = operatorIdMasked;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getOriginalChannelCode() {
        return originalChannelCode;
    }

    public void setOriginalChannelCode(String originalChannelCode) {
        this.originalChannelCode = originalChannelCode;
    }

    public String getClientAppId() {
        return clientAppId;
    }

    public void setClientAppId(String clientAppId) {
        this.clientAppId = clientAppId;
    }

    public String getCallerService() {
        return callerService;
    }

    public void setCallerService(String callerService) {
        this.callerService = callerService;
    }

    public String getExternalInstitutionCode() {
        return externalInstitutionCode;
    }

    public void setExternalInstitutionCode(String externalInstitutionCode) {
        this.externalInstitutionCode = externalInstitutionCode;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
