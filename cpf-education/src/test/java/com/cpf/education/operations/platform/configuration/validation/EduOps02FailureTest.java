package com.cpf.education.operations.platform.configuration.validation;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-02 FailureTest — Profile·환경변수·설정값 전체 검증 */
public final class EduOps02FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps02Handler(); }
}
