package com.cpf.reference.optional.operations.command;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-03 FailureTest — 안전한 운영 조치 */
public final class EduAdm03FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm03Handler(); }
}
