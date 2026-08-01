package com.cpf.reference.online.query.cursor;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-16 FailureTest — 대용량 목록 검색·정렬·Cursor Paging */
public final class EduDev16FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev16Handler(); }
}
