package com.cpf.education.scenarios.online.cache.consistency;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-36 FailureTest — Cache Stampede·Negative Cache·원본 정합성 */
public final class EduDev36FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev36Handler(); }
}
