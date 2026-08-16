package com.cpf.education.operations.gateway.reconcile;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-06 FailureTest — Attempt Ledger·UNKNOWN_RESULT·LKG 복구 */
public final class EduGw06FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw06Handler(); }
}
