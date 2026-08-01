package com.cpf.reference.optional.backoffice.evidence;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-06 FailureTest — 첨부·알림·감사·다운로드 */
public final class EduBackoffice06FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice06Handler(); }
}
