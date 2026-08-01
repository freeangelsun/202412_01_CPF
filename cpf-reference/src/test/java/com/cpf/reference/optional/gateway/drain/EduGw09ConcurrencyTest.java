package com.cpf.reference.optional.gateway.drain;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-09 ConcurrencyTest — Header 정리·경로·요청·응답 변환 */
public final class EduGw09ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw09Handler(); }
}
