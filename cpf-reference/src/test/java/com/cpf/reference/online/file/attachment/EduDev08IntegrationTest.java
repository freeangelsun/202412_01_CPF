package com.cpf.reference.online.file.attachment;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-08 IntegrationTest — 파일 업로드·검사·첨부·다운로드 */
public final class EduDev08IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev08Handler(); }
}
