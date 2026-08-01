package com.cpf.reference.optional.gateway.health;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-08 ConcurrencyTest — SSRF Allowlist·DNS Rebinding·내부망 차단 */
public final class EduGw08ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw08Handler(); }
}
