package com.cpf.education.operations.gateway.reconcile;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-06 IntegrationTest — Attempt Ledger·UNKNOWN_RESULT·LKG 복구 */
public final class EduGw06IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw06Handler(); }
}
