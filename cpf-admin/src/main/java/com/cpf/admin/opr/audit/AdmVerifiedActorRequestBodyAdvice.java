package com.cpf.admin.opr.audit;

import com.cpf.core.api.error.CpfValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Legacy DTO에 운영자 식별 필드가 남아 있어도 검증된 인증 주체와 다르면 거부하여 actor spoofing을 차단합니다.
 * 신규 API는 이 필드를 요청 모델에 두지 않고 {@code adm.operatorId} 서버 Context를 직접 사용합니다.
 */
@ControllerAdvice(basePackages="com.cpf.admin.opr.controller")
public class AdmVerifiedActorRequestBodyAdvice extends RequestBodyAdviceAdapter {
    @Override public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType){return true;}
    @Override public Object afterBodyRead(Object body,HttpInputMessage inputMessage,MethodParameter parameter,Type targetType,Class<? extends HttpMessageConverter<?>> converterType){
        HttpServletRequest request=currentRequest(); if(request==null||!isMutation(request))return body;
        String claimed=claimedActor(body); if(claimed==null||claimed.isBlank())return body;
        Object verified=request.getAttribute("adm.operatorId");
        if(!(verified instanceof String actor)||actor.isBlank()||!actor.trim().equals(claimed.trim())){
            throw new CpfValidationException("요청 본문의 운영자 식별자는 인증된 ADM 운영자와 일치해야 합니다.");
        }
        return body;
    }
    private static String claimedActor(Object body){
        if(body==null)return null;
        for(String name:new String[]{"requestUser","getRequestUser","requestedBy","getRequestedBy","actorId","getActorId","operatorIdOverride","getOperatorIdOverride"})
            try{Method m=body.getClass().getMethod(name);if(m.getParameterCount()==0){Object v=m.invoke(body);if(v!=null&&!String.valueOf(v).isBlank())return String.valueOf(v);}}catch(ReflectiveOperationException ignored){}
        return null;
    }
    private static HttpServletRequest currentRequest(){var a=RequestContextHolder.getRequestAttributes();return a instanceof ServletRequestAttributes s?s.getRequest():null;}
    private static boolean isMutation(HttpServletRequest r){String m=r.getMethod();return "POST".equals(m)||"PUT".equals(m)||"PATCH".equals(m)||"DELETE".equals(m);}
}
