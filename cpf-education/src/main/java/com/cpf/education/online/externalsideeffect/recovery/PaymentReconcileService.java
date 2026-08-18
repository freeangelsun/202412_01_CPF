package com.cpf.education.online.externalsideeffect.recovery;
import com.cpf.core.api.result.CpfRecoveryInfo;
/** PaymentReconcileService는 UNKNOWN 결과를 Probe/Reconcile/Recovery 경로로 수렴시키는 복구 Golden Path입니다. */
public final class PaymentReconcileService { public CpfRecoveryInfo recovery(String key){return new CpfRecoveryInfo("payment:"+key,"PROBE_OR_RECONCILE");} }
