package com.cpf.education.operations.gateway.health;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-08 RecoveryTest — SSRF Allowlist·DNS Rebinding·내부망 차단 */
public final class EduGw08RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw08Handler(); }
}
