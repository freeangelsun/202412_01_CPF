package com.cpf.security.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cpf.core.api.context.CpfContext;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.security.api.audit.CpfAuthorizationAuditSink;
import java.lang.reflect.Method;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 온라인 거래 권한의 정확 일치와 감사 사유 fail-closed 정책을 검증합니다. */
class CpfOnlineTransactionSecurityPolicyEvaluatorTest {
    @AfterEach void clear(){SecurityContextHolder.clearContext();RequestContextHolder.resetRequestAttributes();}
    @Test void exactPermissionAndAuditReasonAreConsumed() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("u","n/a",List.of(new SimpleGrantedAuthority("MEMBER.WRITE"))));
        var request=new MockHttpServletRequest();request.addHeader(CpfOnlineTransactionSecurityPolicyEvaluator.AUDIT_REASON_HEADER,"customer-request");RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var sink=mock(CpfAuthorizationAuditSink.class);var e=new CpfOnlineTransactionSecurityPolicyEvaluator(sink,Clock.fixed(Instant.EPOCH,ZoneOffset.UTC));
        e.verify(rule(),context());
        assertEquals("customer-request",request.getAttribute(CpfOnlineTransactionSecurityPolicyEvaluator.AUDIT_REASON_ATTRIBUTE));verify(sink).record(argThat(v->v.allowed()));
    }
    @Test void substringPermissionNeverPasses() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("u","n/a",List.of(new SimpleGrantedAuthority("MEMBER.WRITE.ALL"))));
        var e=new CpfOnlineTransactionSecurityPolicyEvaluator(mock(CpfAuthorizationAuditSink.class),Clock.systemUTC());
        assertThrows(SecurityException.class,()->e.verify(rule(),context()));
    }
    private static CpfOnlineTransaction rule() throws Exception {return Sample.class.getDeclaredMethod("run").getAnnotation(CpfOnlineTransaction.class);}
    private static CpfContext context(){
        Instant now=Instant.EPOCH;return new CpfContext(new CpfContext.CpfTransactionContext("tx","tx",null,null,null,"API","TEST",LocalDate.of(2026,8,11),now,CpfContext.CpfTransactionOriginKind.HTTP,"TEST","tx"),new CpfContext.CpfExecutionContext(null,"ex","ex",null,"seg",null,CpfContext.CpfExecutionType.API,1,0,now,null,CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),null,null,null);
    }
    /** Sample 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    static class Sample {@CpfOnlineTransaction(id="MBR_SECURED_TX",name="secured",ownerDomain="MBR",requiredPermission="MEMBER.WRITE",auditReasonRequired=true) void run(){}}
}
