package com.cpf.reference.online.cache.consistency;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-36 UnitTest — Cache Stampede·Negative Cache·원본 정합성 */
public final class EduDev36UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev36Handler(); }
}
