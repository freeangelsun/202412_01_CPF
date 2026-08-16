package com.cpf.education.scenarios.online.cache.consistency;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-36 UnitTest — Cache Stampede·Negative Cache·원본 정합성 */
public final class EduDev36UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev36Handler(); }
}
