package com.cpf.starter.platform.operations.health;
import com.cpf.core.api.health.CpfDrainControl; import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.IOException; import org.springframework.web.filter.OncePerRequestFilter;
public final class CpfDrainWebFilter extends OncePerRequestFilter {
 private final CpfDrainControl drain; public CpfDrainWebFilter(CpfDrainControl drain){this.drain=drain;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{ if(!drain.tryEnter()){res.sendError(503,"instance draining");return;} try{chain.doFilter(req,res);}finally{drain.leave();} }
}
