package com.cpf.education.operations.gateway.version;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-11 FailureTest — Command 멱등성·Attempt Ledger·응답 유실 */
public final class EduGw11FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw11Handler(); }
}
