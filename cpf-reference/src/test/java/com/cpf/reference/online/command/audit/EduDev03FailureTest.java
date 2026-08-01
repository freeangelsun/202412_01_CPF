package com.cpf.reference.online.command.audit;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-03 FailureTest — 등록·수정·상태 변경과 감사 */
public final class EduDev03FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev03Handler(); }
}
