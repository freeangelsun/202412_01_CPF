package com.cpf.reference.online.runtime.featuretoggle;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-35 FailureTest — 기능 전환 Canary·Kill Switch·사용자 Segment */
public final class EduDev35FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev35Handler(); }
}
