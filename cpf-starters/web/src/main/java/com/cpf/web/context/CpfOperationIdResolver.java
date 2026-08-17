package com.cpf.web.context;

import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** @CpfOnlineTransaction/OpenAPI/Runtime Header가 공유하는 단일 operationId resolver입니다. */
public final class CpfOperationIdResolver {
    public String resolve(HandlerMethod handlerMethod) {
        if (handlerMethod == null) throw new IllegalArgumentException("handlerMethod is required");
        CpfOnlineTransaction tx=AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(),CpfOnlineTransaction.class);
        if(tx==null)tx=AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(),CpfOnlineTransaction.class);
        Operation openApi=AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(),Operation.class);
        String txId=tx==null?null:trim(tx.operationId());
        String openApiId=openApi==null?null:trim(openApi.operationId());
        if(txId!=null&&openApiId!=null&&!txId.equals(openApiId))throw new IllegalStateException("CPF_OPERATION_ID_OPENAPI_MISMATCH:"+txId+":"+openApiId+":"+handlerMethod);
        if(txId!=null)return txId;
        if(openApiId!=null)return openApiId;
        String controller=handlerMethod.getBeanType().getSimpleName().replaceFirst("Controller$","");
        String method=capitalize(handlerMethod.getMethod().getName());
        String parameters=Arrays.stream(handlerMethod.getMethod().getParameterTypes()).map(Class::getSimpleName).map(this::capitalize).collect(Collectors.joining("And"));
        String base=lowerFirst(controller)+method; return parameters.isBlank()?base:base+"Using"+parameters;
    }
    private static String trim(String v){return v==null||v.isBlank()?null:v.trim();}
    private String capitalize(String value){if(value==null||value.isBlank())return "Value";return value.substring(0,1).toUpperCase(Locale.ROOT)+value.substring(1);}
    private String lowerFirst(String value){if(value==null||value.isBlank())return "controller";return value.substring(0,1).toLowerCase(Locale.ROOT)+value.substring(1);}
}
