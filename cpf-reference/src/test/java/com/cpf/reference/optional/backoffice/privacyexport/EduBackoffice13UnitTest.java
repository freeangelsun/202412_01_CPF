package com.cpf.reference.optional.backoffice.privacyexport;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-13 UnitTest — 개인정보 Masking·감사 조회·승인 Export */
public final class EduBackoffice13UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice13Handler(); }
}
