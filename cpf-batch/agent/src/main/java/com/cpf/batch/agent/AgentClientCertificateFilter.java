package com.cpf.batch.agent;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.*;

@Component
public class AgentClientCertificateFilter extends OncePerRequestFilter {
    private final AgentProperties properties; private final boolean production;
    public AgentClientCertificateFilter(AgentProperties properties,Environment environment){
        this.properties=properties;this.production=Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase);
    }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException {
        if(!production){chain.doFilter(request,response);return;}
        Object attribute=request.getAttribute("jakarta.servlet.request.X509Certificate");
        X509Certificate[] certs=attribute instanceof X509Certificate[] x?x:null;
        if(certs==null||certs.length==0){response.sendError(401,"mTLS client certificate required");return;}
        String subject=certs[0].getSubjectX500Principal().getName();
        if(properties.getAllowedClientSubjects().stream().noneMatch(subject::equals)){
            response.sendError(403,"Client certificate subject is not approved");return;
        }
        chain.doFilter(request,response);
    }
}
