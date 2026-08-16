package com.cpf.education.operations.gateway.reconcile;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-06 RecoveryTest — Attempt Ledger·UNKNOWN_RESULT·LKG 복구 */
public final class EduGw06RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw06Handler(); }
}
