package com.cpf.education.operations.backoffice.evidence;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-06 UnitTest — 첨부·알림·감사·다운로드 */
public final class EduBackoffice06UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice06Handler(); }
}
