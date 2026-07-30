package com.cpf.admin.opr.batch.runtime;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.cpf.core.api.servicecall.CpfServiceRequest;
import com.cpf.core.api.servicecall.CpfServiceResult;
import com.cpf.core.api.util.CpfHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/** ADM은 Service Registry 기반 Typed ServiceCall로 BAT Control Server Owner API만 호출합니다. */
@Component
public class BatchRuntimeControlClient {
    private static final String SERVICE_ID="BAT";
    private static final String ENDPOINT_CODE="SBATCT0001";
    private final CpfServiceCaller caller;
    private final WebClient webClient;
    private final AdmAuthenticatedOperatorContext operatorContext;
    private final String callerInstanceId;

    public BatchRuntimeControlClient(CpfServiceCaller caller,WebClient.Builder builder,
            AdmAuthenticatedOperatorContext operatorContext,
            @Value("${cpf.framework.instance-id:adm-local-01}")String callerInstanceId) {
        this.caller=caller;this.webClient=builder.build();this.operatorContext=operatorContext;
        this.callerInstanceId=required(callerInstanceId,"callerInstanceId");
    }

    public List<Map<String,Object>> instances(long staleAfterSeconds){return list(invoke(HttpMethod.GET,"/api/v1/batch/runtime/instances?staleAfterSeconds="+Math.max(5,staleAfterSeconds),null));}
    public Map<String,Object> view(String view){return map(invoke(HttpMethod.GET,"/api/v1/batch/views/"+encode(view),null));}
    public Map<String,Object> createPlan(Map<String,Object> request){return map(invoke(HttpMethod.POST,"/api/v1/batch/deployment-plans",request));}
    public List<Map<String,Object>> jobDefinitions(String jobId,String state,int limit){
        StringBuilder path=new StringBuilder("/api/v1/batch/job-definitions?limit=").append(Math.max(1,Math.min(limit,1000)));
        if(text(jobId))path.append("&jobId=").append(encode(jobId));if(text(state))path.append("&state=").append(encode(state));
        return list(invoke(HttpMethod.GET,path.toString(),null));
    }
    public Map<String,Object> jobDefinitionDetail(String jobId,long version){return map(invoke(HttpMethod.GET,"/api/v1/batch/job-definitions/"+encode(jobId)+"/versions/"+version,null));}
    public Map<String,Object> validateJobDefinition(Map<String,Object> request){return map(invoke(HttpMethod.POST,"/api/v1/batch/job-definitions/validate",request));}
    public Map<String,Object> saveJobDefinition(Map<String,Object> request){return map(invoke(HttpMethod.POST,"/api/v1/batch/job-definitions/drafts",request));}
    public Map<String,Object> transitionJobDefinition(String jobId,long version,Map<String,Object> request){return map(invoke(HttpMethod.POST,"/api/v1/batch/job-definitions/"+encode(jobId)+"/versions/"+version+"/transition",request));}

    private Object invoke(HttpMethod method,String path,Object payload) {
        String actor=required(operatorContext.currentOperatorId(),"authenticated operator");
        CpfServiceRequest request=CpfServiceRequest.builder(SERVICE_ID).endpointCode(ENDPOINT_CODE)
                .httpMethod(method.name()).requestPath(path).header(CpfHeaders.callerService(),"ADM")
                .header(CpfHeaders.callerInstanceId(),callerInstanceId).header(CpfHeaders.operatorId(),actor)
                .attribute("ownerDomain","BAT").attribute("callerDomain","ADM").build();
        CpfServiceResult<Object> result=caller.invoke(request,target->{
            try {
                WebClient.RequestBodySpec call=webClient.method(method).uri(join(target.baseUrl(),path))
                        .headers(h->{h.set(CpfHeaders.callerService(),"ADM");h.set(CpfHeaders.callerInstanceId(),callerInstanceId);h.set(CpfHeaders.operatorId(),actor);});
                return (payload==null?call:call.bodyValue(payload)).retrieve().bodyToMono(Object.class).block();
            } catch(WebClientResponseException ex) {
                throw ownerHttp(ex);
            }
        });
        if(result.unknown())throw new BatchControlClientException(BatchControlClientException.Category.UNKNOWN_RESULT,
                value(result.failureCode(),"BAT_CONTROL_UNKNOWN"),"BAT Owner 호출 결과를 확정할 수 없습니다.",null,null);
        if(!result.success())throw new BatchControlClientException(category(result.failureCode()),
                value(result.failureCode(),"BAT_CONTROL_FAILED"),value(result.failureMessage(),"BAT Owner 호출 실패"),null,null);
        return result.responseBody();
    }

    private BatchControlClientException ownerHttp(WebClientResponseException ex){
        int status=ex.getStatusCode().value();
        BatchControlClientException.Category category=status==400?BatchControlClientException.Category.VALIDATION:
                status==403?BatchControlClientException.Category.PERMISSION:status==404?BatchControlClientException.Category.NOT_FOUND:
                status==409?BatchControlClientException.Category.CONFLICT:status>=500?BatchControlClientException.Category.UNAVAILABLE:
                BatchControlClientException.Category.OWNER_ERROR;
        return new BatchControlClientException(category,"BAT_OWNER_HTTP_"+status,"BAT Owner HTTP 오류: "+status,null,ex);
    }
    private static BatchControlClientException.Category category(String code){String v=value(code,"").toUpperCase();if(v.contains("VALID"))return BatchControlClientException.Category.VALIDATION;if(v.contains("CONFLICT")||v.contains("VERSION"))return BatchControlClientException.Category.CONFLICT;if(v.contains("PERMISSION")||v.contains("AUTH"))return BatchControlClientException.Category.PERMISSION;if(v.contains("NOT_FOUND"))return BatchControlClientException.Category.NOT_FOUND;if(v.contains("UNAVAILABLE")||v.contains("TIMEOUT")||v.contains("TARGET_DOWN"))return BatchControlClientException.Category.UNAVAILABLE;return BatchControlClientException.Category.OWNER_ERROR;}
    private static String join(String base,String path){if(!text(base))throw new BatchControlClientException(BatchControlClientException.Category.UNAVAILABLE,"BAT_SERVICE_UNAVAILABLE","BAT Service Registry baseUrl이 없습니다.",null,null);return base.endsWith("/")?base.substring(0,base.length()-1)+path:base+path;}
    private static String encode(String v){return java.net.URLEncoder.encode(value(v,""),java.nio.charset.StandardCharsets.UTF_8);}
    private static boolean text(String v){return v!=null&&!v.isBlank();}
    private static String required(String v,String f){if(!text(v))throw new IllegalStateException(f+" is required");return v.trim();}
    private static String value(String v,String d){return text(v)?v.trim():d;}
    @SuppressWarnings("unchecked")private static List<Map<String,Object>> list(Object v){return v==null?List.of():(List<Map<String,Object>>)v;}
    @SuppressWarnings("unchecked")private static Map<String,Object> map(Object v){return v==null?Map.of():(Map<String,Object>)v;}
}
