package com.cpf.reference.optional.operations.bulk;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-ADM-10 FailureTest — 대상 일괄 조치·부분 성공·결과 파일 */
public final class EduAdm10FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm10Handler(); }
}
