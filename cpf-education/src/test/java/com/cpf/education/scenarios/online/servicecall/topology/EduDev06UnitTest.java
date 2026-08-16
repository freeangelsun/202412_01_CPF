package com.cpf.education.scenarios.online.servicecall.topology;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-06 UnitTest — 같은 애플리케이션·분리 서비스 호출 동등성 */
public final class EduDev06UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev06Handler(); }
}
