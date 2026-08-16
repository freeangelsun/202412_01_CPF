package com.cpf.education.operations.gateway.reconcile;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-06 UnitTest — Attempt Ledger·UNKNOWN_RESULT·LKG 복구 */
public final class EduGw06UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw06Handler(); }
}
