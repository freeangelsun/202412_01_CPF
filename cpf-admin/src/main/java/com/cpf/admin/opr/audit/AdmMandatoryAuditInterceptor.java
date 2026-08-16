package com.cpf.admin.opr.audit;

import com.cpf.admin.opr.service.AdmAuditDeliveryService;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.context.CpfContexts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 모든 ADM 변경 API가 Owner 작업 전에 durable audit reservation을 갖도록 강제합니다. */
@Component
public class AdmMandatoryAuditInterceptor implements HandlerInterceptor {
    private final AdmAuditDeliveryService deliveryService;
    public AdmMandatoryAuditInterceptor(AdmAuditDeliveryService deliveryService){this.deliveryService=deliveryService;}
    @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler){
        AdmMandatoryAuditContext.clear(); if(!protectedMutation(request))return true;
        Object a=request.getAttribute("adm.operatorId");
        if(!(a instanceof String operatorId)||operatorId.isBlank()) throw new CpfValidationException("검증된 ADM 운영자 ID가 없어 변경 요청을 수행할 수 없습니다.");
        String claimed=request.getParameter("requestUser");
        if(claimed!=null&&!claimed.isBlank()&&!operatorId.trim().equals(claimed.trim())) throw new CpfValidationException("requestUser query parameter는 인증된 ADM 운영자와 일치해야 합니다.");
        String target=request.getMethod()+" "+request.getRequestURI(); if(target.length()>100)target=target.substring(0,100);
        long id=deliveryService.reserve(new AdmAuditDeliveryService.AuditCommand(CpfContexts.transactionId(),CpfContexts.traceId(),operatorId.trim(),"HTTP_MUTATION","ADM_API",target,"ADM mutation mandatory reservation",null,request.getRemoteAddr()));
        AdmMandatoryAuditContext.begin(id); return true;
    }
    @Override public void afterCompletion(HttpServletRequest request,HttpServletResponse response,Object handler,Exception ex){
        Long id=AdmMandatoryAuditContext.deliveryId();
        try { if(id!=null&&!AdmMandatoryAuditContext.completed()){
            boolean ok=ex==null&&response.getStatus()<400;
            deliveryService.completeOperation(id,ok?"SUCCEEDED":"FAILED","httpStatus="+response.getStatus(),ex==null?null:"handlerException="+ex.getClass().getSimpleName());
        }} finally {AdmMandatoryAuditContext.clear();}
    }
    private boolean protectedMutation(HttpServletRequest request){
        String m=request.getMethod(); if(!("POST".equals(m)||"PUT".equals(m)||"PATCH".equals(m)||"DELETE".equals(m)))return false;
        String u=request.getRequestURI(); if(u==null||!u.startsWith("/adm/api/"))return false;
        return !("/adm/api/auth/login".equals(u)||"/adm/api/auth/logout".equals(u));
    }
}
