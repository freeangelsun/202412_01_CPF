package com.cpf.reference.optional.gateway.reconcile;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-06 ConcurrencyTest — Attempt Ledger·UNKNOWN_RESULT·LKG 복구 */
public final class EduGw06ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw06Handler(); }
}
