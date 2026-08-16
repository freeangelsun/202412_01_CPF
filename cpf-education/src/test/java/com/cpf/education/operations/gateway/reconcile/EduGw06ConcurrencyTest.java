package com.cpf.education.operations.gateway.reconcile;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-06 ConcurrencyTest — Attempt Ledger·UNKNOWN_RESULT·LKG 복구 */
public final class EduGw06ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw06Handler(); }
}
