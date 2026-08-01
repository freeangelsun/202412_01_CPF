package com.cpf.reference.optional.gateway.reconcile;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-06 FailureTest — Attempt Ledger·UNKNOWN_RESULT·LKG 복구 */
public final class EduGw06FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw06Handler(); }
}
