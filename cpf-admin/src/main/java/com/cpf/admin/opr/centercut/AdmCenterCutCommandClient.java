package com.cpf.admin.opr.centercut;

import com.cpf.core.api.batch.CpfBatchRiskCommand;
import java.util.Map;

/** ADM approval boundary에서 BAT Center-Cut execution-scope 명령을 호출하는 Port입니다. */
public interface AdmCenterCutCommandClient {
    Map<String, Object> reprocessFailed(String executionId, CpfBatchRiskCommand command);
    Map<String, Object> reconcileUnknown(String executionId, CpfBatchRiskCommand command);
    /** Observation-only owner read used to reconcile UNKNOWN without replaying the mutation. */
    Map<String, Object> observe(String executionId);
}
