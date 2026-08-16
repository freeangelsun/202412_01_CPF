package com.cpf.education.operations.platform.upgrade.compatibility;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-15 FailureTest — Version Upgrade·DB 호환·Application Rollback */
public final class EduOps15FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps15Handler(); }
}
