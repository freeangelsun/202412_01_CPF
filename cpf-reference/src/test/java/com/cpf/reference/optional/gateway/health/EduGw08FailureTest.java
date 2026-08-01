package com.cpf.reference.optional.gateway.health;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-08 FailureTest — SSRF Allowlist·DNS Rebinding·내부망 차단 */
public final class EduGw08FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw08Handler(); }
}
