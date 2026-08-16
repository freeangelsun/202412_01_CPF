package com.cpf.education.operations.gateway.drain;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-09 ConcurrencyTest — Header 정리·경로·요청·응답 변환 */
public final class EduGw09ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw09Handler(); }
}
