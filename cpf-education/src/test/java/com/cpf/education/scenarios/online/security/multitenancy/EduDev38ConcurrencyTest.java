package com.cpf.education.scenarios.online.security.multitenancy;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-38 ConcurrencyTest — 다중 Tenant 격리·설정·데이터 범위 */
public final class EduDev38ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev38Handler(); }
}
