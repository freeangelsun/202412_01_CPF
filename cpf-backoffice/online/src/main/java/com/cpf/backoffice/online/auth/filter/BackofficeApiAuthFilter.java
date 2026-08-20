package com.cpf.backoffice.online.auth.filter;

import com.cpf.backoffice.online.auth.permission.BackofficePermissionManifest;
import com.cpf.backoffice.online.auth.service.BackofficeAuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;

/** 화면 표시 여부와 무관하게 모든 MBW API를 Method/Path Action 단위로 재검사합니다. */
@Component
public class BackofficeApiAuthFilter extends OncePerRequestFilter {
    private final BackofficeAuthService authService;
    private final BackofficePermissionManifest manifest;
    public BackofficeApiAuthFilter(BackofficeAuthService authService,BackofficePermissionManifest manifest){this.authService=authService;this.manifest=manifest;}
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){String p=request.getRequestURI();return !p.startsWith("/api/v1/backoffice/")||p.startsWith("/api/v1/backoffice/auth/")||"OPTIONS".equalsIgnoreCase(request.getMethod());}
    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
        try{
            String relative=request.getRequestURI().substring("/api/v1/backoffice/".length());
            var permission=manifest.resolve(request.getMethod(),relative).orElseThrow(()->new ResponseStatusException(HttpStatus.FORBIDDEN,"등록되지 않은 MBW API Resource입니다."));
            var authorization=authService.authorize(request.getHeader(HttpHeaders.AUTHORIZATION),permission.menuCode(),permission.actionCode());
            request.setAttribute("backoffice.operator",authorization.operator());request.setAttribute("backoffice.operatorId",authorization.operator().loginId());request.setAttribute("backoffice.actionCode",permission.actionCode());
            chain.doFilter(request,response);
        }catch(ResponseStatusException ex){response.setStatus(ex.getStatusCode().value());response.setContentType(MediaType.APPLICATION_JSON_VALUE);response.setCharacterEncoding("UTF-8");response
                .getWriter().write("{\"message\":\"MBW 인증 또는 행위 권한 확인에 실패했습니다.\"}");}
    }
}
