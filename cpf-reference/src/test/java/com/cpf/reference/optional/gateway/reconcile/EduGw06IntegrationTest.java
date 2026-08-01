package com.cpf.reference.optional.gateway.reconcile;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-06 IntegrationTest — Attempt Ledger·UNKNOWN_RESULT·LKG 복구 */
public final class EduGw06IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw06Handler(); }
}
