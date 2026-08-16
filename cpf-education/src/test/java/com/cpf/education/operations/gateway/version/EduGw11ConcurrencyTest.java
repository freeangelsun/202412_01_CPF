package com.cpf.education.operations.gateway.version;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-11 ConcurrencyTest — Command 멱등성·Attempt Ledger·응답 유실 */
public final class EduGw11ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw11Handler(); }
}
