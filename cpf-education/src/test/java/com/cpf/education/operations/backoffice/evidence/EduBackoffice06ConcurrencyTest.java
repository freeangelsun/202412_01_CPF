package com.cpf.education.operations.backoffice.evidence;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BZA-06 ConcurrencyTest — 첨부·알림·감사·다운로드 */
public final class EduBackoffice06ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice06Handler(); }
}
