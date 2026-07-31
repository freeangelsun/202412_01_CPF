package com.cpf.starter.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import org.springframework.web.filter.OncePerRequestFilter;

/** Browser 자격증명은 HttpOnly JDBC Session에만 두고 내부 인증 Filter에만 Bearer를 전달합니다. */
public final class CpfBffSessionBridgeFilter extends OncePerRequestFilter {
    public static final String ACCESS_TOKEN = "CPF_BFF_ACCESS_TOKEN";
    public static final String REFRESH_TOKEN = "CPF_BFF_REFRESH_TOKEN";
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path=request.getRequestURI(); return !(path.startsWith("/adm/api/")||path.startsWith("/api/bza/"));
    }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        if (request.getHeader("Authorization") != null) { response.sendError(400,"Browser Authorization header is prohibited"); return; }
        HttpSession session=request.getSession(false); String token=session==null?null:(String)session.getAttribute(ACCESS_TOKEN);
        if(token==null||token.isBlank()){chain.doFilter(request,response);return;}
        chain.doFilter(new HttpServletRequestWrapper(request){
            @Override public String getHeader(String name){return "Authorization".equalsIgnoreCase(name)?"Bearer "+token:super.getHeader(name);}
            @Override public Enumeration<String> getHeaders(String name){return "Authorization".equalsIgnoreCase(name)?Collections.enumeration(Collections.singleton("Bearer "+token)):super.getHeaders(name);}
        },response);
    }
}
