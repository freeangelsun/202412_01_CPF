package com.cpf.reference.online.runtime.featuretoggle;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-35 UnitTest — 기능 전환 Canary·Kill Switch·사용자 Segment */
public final class EduDev35UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev35Handler(); }
}
