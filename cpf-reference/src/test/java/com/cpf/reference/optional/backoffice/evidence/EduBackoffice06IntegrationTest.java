package com.cpf.reference.optional.backoffice.evidence;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BZA-06 IntegrationTest — 첨부·알림·감사·다운로드 */
public final class EduBackoffice06IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBackoffice06Handler(); }
}
