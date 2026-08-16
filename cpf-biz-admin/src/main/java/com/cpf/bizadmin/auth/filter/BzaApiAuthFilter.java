package com.cpf.bizadmin.auth.filter;

import com.cpf.bizadmin.auth.permission.BzaPermissionManifest;
import com.cpf.bizadmin.auth.service.BzaAuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;

/** 화면 표시 여부와 무관하게 모든 BZA API를 Method/Path Action 단위로 재검사합니다. */
@Component
public class BzaApiAuthFilter extends OncePerRequestFilter {
    private final BzaAuthService authService;
    private final BzaPermissionManifest manifest;
    public BzaApiAuthFilter(BzaAuthService authService,BzaPermissionManifest manifest){this.authService=authService;this.manifest=manifest;}
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){String p=request.getRequestURI();return !p.startsWith("/api/bza/")||p.startsWith("/api/bza/auth/")||"OPTIONS".equalsIgnoreCase(request.getMethod());}
    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
        try{
            String relative=request.getRequestURI().substring("/api/bza/".length());
            var permission=manifest.resolve(request.getMethod(),relative).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,"등록되지 않은 BZA API Resource입니다."));
            var authorization=authService.authorize(request.getHeader(HttpHeaders.AUTHORIZATION),permission.menuCode(),permission.actionCode());
            request.setAttribute("bza.operator",authorization.operator());request.setAttribute("bza.operatorId",authorization.operator().loginId());request.setAttribute("bza.actionCode",permission.actionCode());
            chain.doFilter(request,response);
        }catch(ResponseStatusException ex){response.setStatus(ex.getStatusCode().value());response.setContentType(MediaType.APPLICATION_JSON_VALUE);response.setCharacterEncoding("UTF-8");response
                .getWriter().write("{\"message\":\"BZA 인증 또는 행위 권한 확인에 실패했습니다.\"}");}
    }
}
