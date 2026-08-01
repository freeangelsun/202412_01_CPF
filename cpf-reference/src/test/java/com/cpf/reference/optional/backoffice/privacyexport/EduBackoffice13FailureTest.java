package com.cpf.reference.optional.backoffice.privacyexport;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-13 FailureTest — 개인정보 Masking·감사 조회·승인 Export */
public final class EduBackoffice13FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice13Handler(); }
}
