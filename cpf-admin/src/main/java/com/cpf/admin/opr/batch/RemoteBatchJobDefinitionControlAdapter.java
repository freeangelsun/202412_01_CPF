package com.cpf.admin.opr.batch;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.batch.api.BatControlHeaders;
import com.cpf.batch.api.BatchJobDefinitionControlPort;
import com.cpf.integration.api.servicecall.CpfServiceCaller;
import com.cpf.integration.api.servicecall.CpfServiceRequest;
import com.cpf.integration.api.servicecall.CpfServiceResult;
import com.cpf.web.api.CpfHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/** Service Registry를 경유해 BAT Job Definition Owner API를 호출하는 Remote Port입니다. */
public final class RemoteBatchJobDefinitionControlAdapter implements BatchJobDefinitionControlPort {
    private static final String SERVICE_ID="BAT";
    private static final String ENDPOINT_CODE="SBATJD0001";
    private final CpfServiceCaller caller;
    private final WebClient webClient;
    private final AdmAuthenticatedOperatorContext actorContext;
    private final ObjectMapper mapper;
    private final String callerInstanceId;

    public RemoteBatchJobDefinitionControlAdapter(CpfServiceCaller caller,WebClient.Builder builder,
            AdmAuthenticatedOperatorContext actorContext,ObjectMapper mapper,String callerInstanceId) {
        this.caller=caller;this.webClient=builder.build();this.actorContext=actorContext;this.mapper=mapper;
        this.callerInstanceId=required(callerInstanceId,"callerInstanceId");
    }

    @Override
    public DefinitionState state(String jobId,long version) {
        String path="/api/v1/batch/job-definitions/"+encode(jobId)+"/versions/"+version;
        Object body=invoke("GET",path,null,null,null);
        Map<?,?> value=mapper.convertValue(body,Map.class);
        return new DefinitionState(required(String.valueOf(value.get("jobId")),"jobId"),
                longValue(value.get("definitionVersion"),"definitionVersion"),
                required(String.valueOf(value.get("state")),"state"),
                longValue(value.get("rowVersion"),"rowVersion"),
                required(String.valueOf(value.get("checksum")),"checksum"),
                String.valueOf(value.get("requestedBy") == null ? "" : value.get("requestedBy")));
    }

    @Override
    public PublishResult publishApproved(PublishCommand command) {
        String path="/api/v1/batch/job-definitions/"+encode(command.jobId())+"/versions/"+command.definitionVersion()+"/approved-publish";
        Object body=invoke("POST",path,Map.of(
                "operationId",command.operationId(),
                "expectedRowVersion",command.expectedRowVersion(),
                "approvalRequestId",command.approvalRequestId(),
                "payloadHash",command.payloadHash(),
                "requestedBy",command.requestedBy(),
                "approvedBy",command.approvedBy(),
                "reason",command.reason()),
                Long.toString(command.approvalRequestId()), command.requestedBy());
        Map<?,?> value=mapper.convertValue(body,Map.class);
        return new PublishResult(command.jobId(),command.definitionVersion(),
                required(String.valueOf(value.get("state")),"state"),
                longValue(value.get("rowVersion"),"rowVersion"),
                required(String.valueOf(value.get("checksum")),"checksum"),command.operationId());
    }

    private Object invoke(String method,String path,Object payload,
            String approvalRequestId,String approvalRequesterId) {
        String actor=required(actorContext.currentOperatorId(),"authenticated actor");
        CpfServiceRequest.Builder requestBuilder=CpfServiceRequest.builder(SERVICE_ID).endpointCode(ENDPOINT_CODE)
                .httpMethod(method).requestPath(path).header(CpfHeaders.callerService(),"ADM")
                .header(CpfHeaders.callerInstanceId(),callerInstanceId).header(CpfHeaders.operatorId(),actor)
                .attribute("ownerDomain","BAT").attribute("callerDomain","ADM");
        if(approvalRequestId!=null){
            requestBuilder.header(BatControlHeaders.APPROVAL_REQUEST_ID,approvalRequestId);
            requestBuilder.header(BatControlHeaders.APPROVAL_REQUESTER_ID,
                    required(approvalRequesterId,"approvalRequesterId"));
        }
        CpfServiceRequest request=requestBuilder.build();
        CpfServiceResult<Object> result=caller.invoke(request,target->{
            WebClient.RequestBodySpec call=webClient.method(org.springframework.http.HttpMethod.valueOf(method))
                    .uri(join(target.baseUrl(),path)).headers(h->{
                        h.set(CpfHeaders.callerService(),"ADM");
                        h.set(CpfHeaders.callerInstanceId(),callerInstanceId);
                        h.set(CpfHeaders.operatorId(),actor);
                        if(approvalRequestId!=null){
                            h.set(BatControlHeaders.APPROVAL_REQUEST_ID,approvalRequestId);
                            h.set(BatControlHeaders.APPROVAL_REQUESTER_ID,approvalRequesterId);
                        }
                    });
            return (payload==null?call:call.bodyValue(payload)).retrieve().bodyToMono(Object.class).block();
        });
        if(result.unknown())throw new IllegalStateException("BAT_JOB_DEFINITION_UNKNOWN: reconciliation required");
        if(!result.success())throw new IllegalStateException("BAT Job Definition Owner call failed: "+result.failureCode());
        return result.responseBody();
    }
    private static String join(String base,String path){if(base==null||base.isBlank())throw new IllegalStateException("BAT baseUrl unavailable");return base.endsWith("/")?base.substring(0,base.length()-1)+path:base+path;}
    private static String encode(String v){return java.net.URLEncoder.encode(required(v,"jobId"),java.nio.charset.StandardCharsets.UTF_8);}
    private static String required(String v,String f){if(v==null||v.isBlank()||"null".equals(v))throw new IllegalStateException(f+" is required");return v.trim();}
    private static long longValue(Object v,String f){if(v instanceof Number n)return n.longValue();try{return Long.parseLong(String.valueOf(v));}catch(Exception e){throw new IllegalStateException(f+" is invalid",e);}}
}
