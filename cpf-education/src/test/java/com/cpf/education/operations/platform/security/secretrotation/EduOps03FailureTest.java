package com.cpf.education.operations.platform.security.secretrotation;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-03 FailureTest — Secret·Certificate 배포·교체·만료 대응 */
public final class EduOps03FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps03Handler(); }
}
