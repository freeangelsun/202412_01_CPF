package com.cpf.education.operations.backoffice.privacyexport;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-13 ConcurrencyTest — 개인정보 Masking·감사 조회·승인 Export */
public final class EduBackoffice13ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice13Handler(); }
}
