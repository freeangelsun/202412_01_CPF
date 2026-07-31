package com.cpf.starter.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** 기존 인증 서비스 응답의 Token을 Browser Body에서 제거하고 JDBC Session으로 이동합니다. */
@ControllerAdvice
public final class CpfBffCredentialResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override public boolean supports(MethodParameter p,Class<? extends HttpMessageConverter<?>> c){return true;}
    @Override public Object beforeBodyWrite(Object body,MethodParameter p,MediaType m,Class<? extends HttpMessageConverter<?>> c,ServerHttpRequest req,ServerHttpResponse res){
        if(!(req instanceof ServletServerHttpRequest servlet)||!(body instanceof Map<?,?> source))return body;
        String path=servlet.getServletRequest().getRequestURI(); if(!(path.endsWith("/auth/login")||path.endsWith("/auth/refresh")))return body;
        Object access=source.get("accessToken"), refresh=source.get("refreshToken");
        if(!(access instanceof String a)||a.isBlank())return body;
        HttpSession session=servlet.getServletRequest().getSession(true); session.setAttribute(CpfBffSessionBridgeFilter.ACCESS_TOKEN,a);
        if(refresh instanceof String r&&!r.isBlank())session.setAttribute(CpfBffSessionBridgeFilter.REFRESH_TOKEN,r);
        Map<String,Object> sanitized=new LinkedHashMap<>(); source.forEach((k,v)->{if(!"accessToken".equals(k)&&!"refreshToken".equals(k))sanitized.put(String.valueOf(k),v);});
        sanitized.put("sessionId",session.getId()); return sanitized;
    }
}
