package com.cpf.reference.online.cache.consistency;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-36 ConcurrencyTest — Cache Stampede·Negative Cache·원본 정합성 */
public final class EduDev36ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev36Handler(); }
}
