package com.cpf.education.operations.platform.upgrade.compatibility;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-15 ConcurrencyTest — Version Upgrade·DB 호환·Application Rollback */
public final class EduOps15ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps15Handler(); }
}
