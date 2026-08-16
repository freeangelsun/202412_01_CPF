package com.cpf.education.operations.backoffice.privacyexport;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-13 FailureTest — 개인정보 Masking·감사 조회·승인 Export */
public final class EduBackoffice13FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice13Handler(); }
}
