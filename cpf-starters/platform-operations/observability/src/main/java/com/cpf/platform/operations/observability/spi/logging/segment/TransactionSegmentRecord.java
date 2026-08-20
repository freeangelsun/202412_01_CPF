package com.cpf.platform.operations.observability.spi.logging.segment;

import java.time.LocalDateTime;

/**
 * {@code cpf_transaction_segment}에 저장하는 구간 실행 기록입니다.
 */
public class TransactionSegmentRecord {
    private String transactionSegmentId;
    private String transactionId;
    private String executionId;
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
    private String systemCode;
    private String originalSystemCode;
    private String callerSystemCode;
    private String targetSystemCode;
    private String currentChannel;
    private String originalChannel;
    private String clientId;
    private String callerChannel;
    private String targetChannel;
    private String targetOperationId;
    private String externalInstitutionCode;
    private String externalTransactionId;
    private String selectedInstanceId;
    private Integer attemptNo;
    private String retryYn;
    private String failoverYn;
    private String circuitState;
    private Integer downstreamHttpStatus;
    private String resultState;
    private String unknownResultId;
    private String createdBy;
    private String updatedBy;

    public String getTransactionSegmentId() {
        return transactionSegmentId;
    }

    public void setTransactionSegmentId(String transactionSegmentId) {
        this.transactionSegmentId = transactionSegmentId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /** 현재 CPF 실행 인스턴스 ID입니다. 동일 transactionId 내부의 실행/구간 상관관계에 사용합니다. */
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
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

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public String getOriginalSystemCode() {
        return originalSystemCode;
    }

    public void setOriginalSystemCode(String originalSystemCode) {
        this.originalSystemCode = originalSystemCode;
    }

    public String getCallerSystemCode() {
        return callerSystemCode;
    }

    public void setCallerSystemCode(String callerSystemCode) {
        this.callerSystemCode = callerSystemCode;
    }

    public String getTargetSystemCode() {
        return targetSystemCode;
    }

    public void setTargetSystemCode(String targetSystemCode) {
        this.targetSystemCode = targetSystemCode;
    }

    public String getCurrentChannel() {
        return currentChannel;
    }

    public void setCurrentChannel(String currentChannel) {
        this.currentChannel = currentChannel;
    }

    public String getOriginalChannel() {
        return originalChannel;
    }

    public void setOriginalChannel(String originalChannel) {
        this.originalChannel = originalChannel;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCallerChannel() {
        return callerChannel;
    }

    public void setCallerChannel(String callerChannel) {
        this.callerChannel = callerChannel;
    }

    public String getTargetChannel() {
        return targetChannel;
    }

    public void setTargetChannel(String targetChannel) {
        this.targetChannel = targetChannel;
    }

    public String getTargetOperationId() {
        return targetOperationId;
    }

    public void setTargetOperationId(String targetOperationId) {
        this.targetOperationId = targetOperationId;
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

    public String getSelectedInstanceId() {
        return selectedInstanceId;
    }

    public void setSelectedInstanceId(String selectedInstanceId) {
        this.selectedInstanceId = selectedInstanceId;
    }

    public Integer getAttemptNo() {
        return attemptNo;
    }

    public void setAttemptNo(Integer attemptNo) {
        this.attemptNo = attemptNo;
    }

    public String getRetryYn() {
        return retryYn;
    }

    public void setRetryYn(String retryYn) {
        this.retryYn = retryYn;
    }

    public String getFailoverYn() {
        return failoverYn;
    }

    public void setFailoverYn(String failoverYn) {
        this.failoverYn = failoverYn;
    }

    public String getCircuitState() {
        return circuitState;
    }

    public void setCircuitState(String circuitState) {
        this.circuitState = circuitState;
    }

    public Integer getDownstreamHttpStatus() {
        return downstreamHttpStatus;
    }

    public void setDownstreamHttpStatus(Integer downstreamHttpStatus) {
        this.downstreamHttpStatus = downstreamHttpStatus;
    }

    public String getResultState() {
        return resultState;
    }

    public void setResultState(String resultState) {
        this.resultState = resultState;
    }

    public String getUnknownResultId() {
        return unknownResultId;
    }

    public void setUnknownResultId(String unknownResultId) {
        this.unknownResultId = unknownResultId;
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
