package com.cpf.reference.online.api.versioning;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-43 UnitTest — API Version 전환·하위 호환·폐기 */
public final class EduDev43UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev43Handler(); }
}
