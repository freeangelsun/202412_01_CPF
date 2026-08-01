package com.cpf.reference.optional.backoffice.separationofduties;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-10 ConcurrencyTest — 역할 충돌·직무분리·실효 권한 Simulation */
public final class EduBackoffice10ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice10Handler(); }
}
