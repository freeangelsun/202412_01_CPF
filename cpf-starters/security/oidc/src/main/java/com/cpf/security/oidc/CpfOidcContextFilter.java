package com.cpf.security.oidc;

import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import org.springframework.web.filter.OncePerRequestFilter;

/** Binds verified OIDC principal to the existing CPF request context for downstream services. */
public final class CpfOidcContextFilter extends OncePerRequestFilter {
    private final CpfOidcContext current; private final CpfOidcContextBridge bridge; private final Clock clock;
    public CpfOidcContextFilter(CpfOidcContext current,CpfOidcContextBridge bridge,Clock clock){this.current=current;this.bridge=bridge;this.clock=clock;}
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        CpfContextSnapshot parent=CpfContexts.snapshot();
        var principal=current.currentPrincipal();
        if(parent==null||principal.isEmpty()){chain.doFilter(request,response);return;}
        var updated=bridge.apply(parent.context(),principal.get(),clock.instant());
        try(var _=CpfContexts.bind(CpfContextSnapshot.capture(updated,parent.capturedAt()))){
            chain.doFilter(request,response);
        } catch (ServletException | IOException | RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException("CPF OIDC context scope close failed", ex);
        }
    }
}
