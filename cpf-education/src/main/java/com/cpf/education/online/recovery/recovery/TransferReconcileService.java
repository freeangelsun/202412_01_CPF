package com.cpf.education.online.recovery.recovery;
import com.cpf.core.api.result.CpfResult; import com.cpf.foundation.annotation.CpfService;
@CpfService
// 실패·동시성·복구 경계에서도 원래 의미를 잃지 않도록 UNKNOWN 결과를 Probe/Reconcile/Recovery 경로로 수렴시키는 복구 Golden Path의 정책을 유지합니다.
/** TransferReconcileService는 UNKNOWN 결과를 Probe/Reconcile/Recovery 경로로 수렴시키는 복구 Golden Path입니다. */
public class TransferReconcileService { private final TransferResultProbe probe; public TransferReconcileService(TransferResultProbe probe){this.probe=probe;} public CpfResult<String> reconcile(String key){return probe.probe(key);} }
