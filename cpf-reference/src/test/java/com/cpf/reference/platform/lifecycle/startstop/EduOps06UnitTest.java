package com.cpf.reference.platform.lifecycle.startstop;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-06 UnitTest — 기동·종료·Health·Dependency 순서 */
public final class EduOps06UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps06Handler(); }
}
