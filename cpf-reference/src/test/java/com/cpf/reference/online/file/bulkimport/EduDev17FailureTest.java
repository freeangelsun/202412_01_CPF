package com.cpf.reference.online.file.bulkimport;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-17 FailureTest — 대량 등록 사전검증·부분 오류 보고·재업로드 */
public final class EduDev17FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev17Handler(); }
}
