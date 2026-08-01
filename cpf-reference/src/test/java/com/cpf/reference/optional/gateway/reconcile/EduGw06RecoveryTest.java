package com.cpf.reference.optional.gateway.reconcile;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-06 RecoveryTest — Attempt Ledger·UNKNOWN_RESULT·LKG 복구 */
public final class EduGw06RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw06Handler(); }
}
