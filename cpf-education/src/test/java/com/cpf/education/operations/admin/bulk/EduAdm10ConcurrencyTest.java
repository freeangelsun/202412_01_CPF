package com.cpf.education.operations.admin.bulk;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-ADM-10 ConcurrencyTest — 대상 일괄 조치·부분 성공·결과 파일 */
public final class EduAdm10ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduAdm10Handler(); }
}
