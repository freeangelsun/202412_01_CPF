package com.cpf.education.operations.platform.upgrade.compatibility;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-15 UnitTest — Version Upgrade·DB 호환·Application Rollback */
public final class EduOps15UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps15Handler(); }
}
