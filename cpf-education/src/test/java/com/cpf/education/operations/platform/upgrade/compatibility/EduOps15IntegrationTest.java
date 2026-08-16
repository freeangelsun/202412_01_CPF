package com.cpf.education.operations.platform.upgrade.compatibility;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-15 IntegrationTest — Version Upgrade·DB 호환·Application Rollback */
public final class EduOps15IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps15Handler(); }
}
