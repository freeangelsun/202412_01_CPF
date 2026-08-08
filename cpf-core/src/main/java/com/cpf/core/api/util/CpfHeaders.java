package com.cpf.core.api.util;

import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.core.common.header.CpfHeaderNames;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CPF 표준 Header 이름과 안전한 전달 Map 생성을 제공하는 Public API입니다.
 *
 * <p>신규 업무 코드는 문자열 literal을 직접 만들기보다 이 API를 사용하고,
 * 신뢰 경계/검증/마스킹/자동 전파는 Core Header Engine에 맡깁니다.</p>
 */
public final class CpfHeaders {
    private CpfHeaders() {}

    /** CpfHeaders의 transactionId 공개 계약을 수행합니다.

     * @return transactionId에 해당하는 정본 CPF Header 이름

     */

    public static String transactionId() { return CpfHeaderNames.TRANSACTION_ID; }
    /** CpfHeaders의 segmentId 공개 계약을 수행합니다.
     * @return segmentId에 해당하는 정본 CPF Header 이름
     */
    public static String segmentId() { return CpfHeaderNames.TRANSACTION_SEGMENT_ID; }
    /** CpfHeaders의 parentSegmentId 공개 계약을 수행합니다.
     * @return parentSegmentId에 해당하는 정본 CPF Header 이름
     */
    public static String parentSegmentId() { return CpfHeaderNames.PARENT_TRANSACTION_SEGMENT_ID; }
    /** CpfHeaders의 standardExecutionId 공개 계약을 수행합니다.
     * @return standardExecutionId에 해당하는 정본 CPF Header 이름
     */
    public static String standardExecutionId() { return CpfHeaderNames.STANDARD_EXECUTION_ID; }
    /** CpfHeaders의 idempotencyKey 공개 계약을 수행합니다.
     * @return idempotencyKey에 해당하는 정본 CPF Header 이름
     */
    public static String idempotencyKey() { return CpfHeaderNames.IDEMPOTENCY_KEY; }
    /** CpfHeaders의 approvalRequestId 공개 계약을 수행합니다.
     * @return approvalRequestId에 해당하는 정본 CPF Header 이름
     */
    public static String approvalRequestId() { return CpfHeaderNames.APPROVAL_REQUEST_ID; }
    /** CpfHeaders의 approvalRequesterId 공개 계약을 수행합니다.
     * @return approvalRequesterId에 해당하는 정본 CPF Header 이름
     */
    public static String approvalRequesterId() { return CpfHeaderNames.APPROVAL_REQUESTER_ID; }
    /** CpfHeaders의 traceId 공개 계약을 수행합니다.
     * @return traceId에 해당하는 정본 CPF Header 이름
     */
    public static String traceId() { return CpfHeaderNames.TRACE_ID; }
    /** CpfHeaders의 spanId 공개 계약을 수행합니다.
     * @return spanId에 해당하는 정본 CPF Header 이름
     */
    public static String spanId() { return CpfHeaderNames.SPAN_ID; }
    /** CpfHeaders의 channelCode 공개 계약을 수행합니다.
     * @return channelCode에 해당하는 정본 CPF Header 이름
     */
    public static String channelCode() { return CpfHeaderNames.CHANNEL_CODE; }
    /** CpfHeaders의 originalChannelCode 공개 계약을 수행합니다.
     * @return originalChannelCode에 해당하는 정본 CPF Header 이름
     */
    public static String originalChannelCode() { return CpfHeaderNames.ORIGINAL_CHANNEL_CODE; }
    /** CpfHeaders의 userId 공개 계약을 수행합니다.
     * @return userId에 해당하는 정본 CPF Header 이름
     */
    public static String userId() { return CpfHeaderNames.USER_ID; }
    /** CpfHeaders의 operatorId 공개 계약을 수행합니다.
     * @return operatorId에 해당하는 정본 CPF Header 이름
     */
    public static String operatorId() { return CpfHeaderNames.OPERATOR_ID; }
    /** CpfHeaders의 tenantId 공개 계약을 수행합니다.
     * @return tenantId에 해당하는 정본 CPF Header 이름
     */
    public static String tenantId() { return CpfHeaderNames.TENANT_ID; }
    /** CpfHeaders의 callerService 공개 계약을 수행합니다.
     * @return callerService에 해당하는 정본 CPF Header 이름
     */
    public static String callerService() { return CpfHeaderNames.CALLER_SERVICE; }
    /** CpfHeaders의 callerInstanceId 공개 계약을 수행합니다.
     * @return callerInstanceId에 해당하는 정본 CPF Header 이름
     */
    public static String callerInstanceId() { return CpfHeaderNames.CALLER_INSTANCE_ID; }

        /** 업무/Generator가 사용하는 핵심 CPF 표준 Header 이름 목록을 반환합니다.
     * @return null이 아닌 결과 목록
     */
    public static List<String> standardNames() {
        return List.of(
                transactionId(), standardExecutionId(), segmentId(), parentSegmentId(), idempotencyKey(), approvalRequestId(), approvalRequesterId(),
                traceId(), spanId(), originalChannelCode(), channelCode(), userId(), operatorId(), tenantId(),
                callerService(), callerInstanceId());
    }

    /** Canonical transactionId와 선택적인 segment 계층을 immutable Header Map으로 만듭니다.

     * @param transactionId CPF canonical transactionId

     * @return 계약에 따른 결과 Map

     */

    public static Map<String,String> transaction(String transactionId) {
        return transaction(transactionId, null, null);
    }

        /** Canonical transactionId와 선택적인 segment 계층을 immutable Header Map으로 만듭니다.
     * @param transactionId CPF canonical transactionId
     * @param segmentIdValue 선택 transaction segmentId
     * @param parentSegmentIdValue 선택 parent segmentId
     * @return 계약에 따른 결과 Map
     */
    public static Map<String,String> transaction(String transactionId, String segmentIdValue, String parentSegmentIdValue) {
        LinkedHashMap<String,String> headers = new LinkedHashMap<>();
        headers.put(transactionId(), CpfTransactionIds.requireCanonical(transactionId));
        if (CpfStrings.hasText(segmentIdValue)) headers.put(segmentId(), segmentIdValue.trim());
        if (CpfStrings.hasText(parentSegmentIdValue)) headers.put(parentSegmentId(), parentSegmentIdValue.trim());
        return Collections.unmodifiableMap(headers);
    }
}
