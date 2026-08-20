package com.cpf.security.session.jdbc;

import jakarta.servlet.FilterChain;import jakarta.servlet.ServletException;import jakarta.servlet.http.Cookie;import jakarta.servlet.http.HttpServletRequest;import jakarta.servlet.http.HttpServletResponse;import jakarta.servlet.http.HttpSession;import java.io.IOException;import java.security.SecureRandom;import java.util.Base64;import org.springframework.web.filter.OncePerRequestFilter;
/** SameSite Cookie와 Session을 결합한 Double-submit CSRF 방어입니다. */
public final class CpfBffCsrfFilter extends OncePerRequestFilter {
 private static final String ATTR="CPF_CSRF_TOKEN";private static final SecureRandom RNG=new SecureRandom();
 @Override protected boolean shouldNotFilter(HttpServletRequest r){String p=r.getRequestURI();return !(p.startsWith("/adm/"));}
 @Override protected void doFilterInternal(HttpServletRequest r,HttpServletResponse s,FilterChain c)throws ServletException,IOException{
  HttpSession session=r.getSession(true);String token=(String)session.getAttribute(ATTR);if(token==null){byte[] b=new byte[32];RNG.nextBytes(b);token=Base64.getUrlEncoder().withoutPadding().encodeToString(b);session.setAttribute(ATTR,token);}
  s.addHeader("Set-Cookie","XSRF-TOKEN="+token+"; Path=/; Secure; SameSite=Strict");
  if(!isSafe(r.getMethod())&&!constant(token,r.getHeader("X-XSRF-TOKEN"))){s.sendError(403,"CSRF token mismatch");return;}c.doFilter(r,s);
 }
 private static boolean isSafe(String m){return "GET".equals(m)||"HEAD".equals(m)||"OPTIONS".equals(m);}
 private static boolean constant(String a,String b){if(b==null||a.length()!=b.length())return false;int x=0;for(int i=0;i<a.length();i++)x|=a.charAt(i)^b.charAt(i);return x==0;}
}
