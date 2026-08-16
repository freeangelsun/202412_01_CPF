package com.cpf.education.operations.gateway.publish;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-05 FailureTest — Draft·검증·승인·게시·부분 적용 */
public final class EduGw05FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw05Handler(); }
}
