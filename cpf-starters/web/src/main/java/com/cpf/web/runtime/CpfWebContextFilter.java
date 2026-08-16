package com.cpf.web.runtime;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.cpf.web.context.CpfHttpInboundContextAdapter;
import com.cpf.web.context.CpfHttpIngressMetadata;
import com.cpf.web.context.CpfHttpIngressTrust;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.filter.OncePerRequestFilter;

/** 요청마다 하나의 root Context를 생성하고 종료 시 lexical scope를 복원합니다. */
public final class CpfWebContextFilter extends OncePerRequestFilter {
    private final CpfHttpInboundContextAdapter inbound; private final CpfBusinessDateProvider businessDates;
    public CpfWebContextFilter(CpfHttpInboundContextAdapter inbound,CpfBusinessDateProvider businessDates){this.inbound=inbound;this.businessDates=businessDates;}
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
        Map<String,String> h=new LinkedHashMap<>();Enumeration<String> names=request.getHeaderNames();if(names!=null)while(names.hasMoreElements()){String n=names.nextElement();h.put(n,request.getHeader(n));}
        var edge=new CpfHttpIngressMetadata(null,null,null,request.getRemoteAddr(),null,null);
        var resolved=inbound.resolve(h,CpfHttpIngressTrust.UNTRUSTED_EXTERNAL,null,null,edge,request.getMethod()+" "+request.getRequestURI(),businessDates.currentBusinessDate(),null);
        try(AutoCloseable ignored=CpfContexts.bind(resolved.snapshot())){chain.doFilter(request,response);}catch(ServletException|IOException|RuntimeException e){throw e;}catch(Exception e){throw new ServletException("CPF context close failed",e);}
    }
}
