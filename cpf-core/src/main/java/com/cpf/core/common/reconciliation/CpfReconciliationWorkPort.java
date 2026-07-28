package com.cpf.core.common.reconciliation;
import java.time.Instant;import java.util.List;
/** Unknown result 자동 확인의 분산 claim과 후속 상태 변경 계약입니다. */
public interface CpfReconciliationWorkPort {
 List<WorkItem> claim(String unknownType,int thresholdSeconds,int limit,String workerId,int leaseSeconds);
 void defer(String unknownId,String workerId,Instant nextCheckAt,String nextAction);
 void markManualReview(String unknownId,String workerId,String nextAction);
 record WorkItem(CpfUnknownResultRecord record,int attemptCount,long rowVersion){}
}
