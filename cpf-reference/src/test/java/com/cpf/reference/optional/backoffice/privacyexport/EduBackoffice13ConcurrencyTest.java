package com.cpf.reference.optional.backoffice.privacyexport;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-13 ConcurrencyTest — 개인정보 Masking·감사 조회·승인 Export */
public final class EduBackoffice13ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice13Handler(); }
}
