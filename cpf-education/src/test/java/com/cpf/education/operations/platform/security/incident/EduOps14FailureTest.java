package com.cpf.education.operations.platform.security.incident;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-14 FailureTest — 보안 사고·계정·키·세션 긴급 차단 */
public final class EduOps14FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps14Handler(); }
}
