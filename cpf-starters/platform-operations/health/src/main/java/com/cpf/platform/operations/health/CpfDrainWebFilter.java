package com.cpf.platform.operations.health;
import com.cpf.platform.operations.api.health.CpfDrainControl;
import jakarta.servlet.*; import jakarta.servlet.http.HttpServletResponse; import java.io.IOException;
/** Drain 중 신규 HTTP 요청을 503으로 거부하고 in-flight를 정확히 계수합니다. */
public final class CpfDrainWebFilter implements Filter {
    private final CpfDrainControl drain;
    public CpfDrainWebFilter(CpfDrainControl drain){this.drain=drain;}
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(!drain.tryEnter()){ if(response instanceof HttpServletResponse http){http.setStatus(503);} return; }
        try{chain.doFilter(request,response);} finally{drain.leave();}
    }
}
