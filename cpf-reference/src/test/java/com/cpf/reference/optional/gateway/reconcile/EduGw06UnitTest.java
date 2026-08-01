package com.cpf.reference.optional.gateway.reconcile;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-06 UnitTest — Attempt Ledger·UNKNOWN_RESULT·LKG 복구 */
public final class EduGw06UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw06Handler(); }
}
