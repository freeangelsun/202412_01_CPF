package com.cpf.reference.platform.upgrade.compatibility;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-15 UnitTest — Version Upgrade·DB 호환·Application Rollback */
public final class EduOps15UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps15Handler(); }
}
