package com.cpf.education.operations.gateway.health;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-08 UnitTest — SSRF Allowlist·DNS Rebinding·내부망 차단 */
public final class EduGw08UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw08Handler(); }
}
