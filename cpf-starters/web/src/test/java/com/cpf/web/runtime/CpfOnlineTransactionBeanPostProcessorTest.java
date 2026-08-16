package com.cpf.web.runtime;

import static org.junit.jupiter.api.Assertions.*;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import org.junit.jupiter.api.Test;

/** 온라인 거래 Annotation의 startup fail-fast 검증을 담당합니다. */
class CpfOnlineTransactionBeanPostProcessorTest {
    @Test void duplicateTransactionIdFailsAtStartup() {
        var p=new CpfOnlineTransactionBeanPostProcessor(); p.postProcessBeforeInitialization(new A(),"a");
        assertThrows(IllegalStateException.class,()->p.postProcessBeforeInitialization(new B(),"b"));
    }
    @Test void invalidVisibilityFailsAtStartup() {
        assertThrows(IllegalStateException.class,()->new CpfOnlineTransactionBeanPostProcessor().postProcessBeforeInitialization(new Invalid(),"invalid"));
    }
    static class A {@CpfOnlineTransaction(id="MBR_DUP_TX",name="a",ownerDomain="MBR") void run(){}}
    /** B 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    static class B {@CpfOnlineTransaction(id="MBR_DUP_TX",name="b",ownerDomain="MBR") void run(){}}
    static class Invalid {@CpfOnlineTransaction(id="MBR_BAD_TX",name="bad",ownerDomain="MBR",visibility="public") void run(){}}
}
