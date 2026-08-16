package com.cpf.education.operations.gateway.version;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-11 RecoveryTest — Command 멱등성·Attempt Ledger·응답 유실 */
public final class EduGw11RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw11Handler(); }
}
