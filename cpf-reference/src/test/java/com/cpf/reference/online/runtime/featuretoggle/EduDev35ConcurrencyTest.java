package com.cpf.reference.online.runtime.featuretoggle;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-35 ConcurrencyTest — 기능 전환 Canary·Kill Switch·사용자 Segment */
public final class EduDev35ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev35Handler(); }
}
