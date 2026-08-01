package com.cpf.reference.edu.runtime.consumer.http;
import com.cpf.reference.edu.runtime.application.*;
import com.cpf.reference.edu.runtime.consumer.*;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
/** Fail-closed HTTP consumer for the independent cpf-reference counterparty simulator or an explicitly configured external institution. */
public final class HttpEduBusinessConsumer implements EduBusinessConsumer {
    private final HttpClient client; private final ObjectMapper json; private final Environment environment;
    public HttpEduBusinessConsumer(HttpClient client,ObjectMapper json,Environment environment){this.client=client;this.json=json;this.environment=environment;}
    @Override public EduConsumerType type(){return EduConsumerType.HTTP;}
    @Override public EduBusinessConsumerResult invoke(EduConsumerBinding b,EduExecutionCommand c,long fence){
        try{
            String base=environment.getProperty(b.configurationKey());if(base==null||base.isBlank())throw new EduValidationException("consumer base URL property is required: "+b.configurationKey());
            URI uri=URI.create(base.replaceAll("/+$","")+"/"+b.entryPoint().replaceAll("^/+",""));if(!List.of("http","https").contains(uri.getScheme()))throw new EduValidationException("unsupported consumer URL scheme");
            String body=json.writeValueAsString(Map.of("requirementId",b.requirementId(),"businessKey",c.businessKey(),"expectedVersion",c.expectedVersion(),"dataScope",c.dataScope(),"requestReason",c.requestReason(),"payload",c.payload(),"fencingToken",fence));
            HttpRequest req=HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(b.timeoutSeconds())).header("Content-Type","application/json").header("X-Cpf-Requirement-Id",b.requirementId()).header("X-Cpf-Request-Id",c.requestId()).header("X-Cpf-Trace-Id",c.traceId()).header("X-Cpf-Idempotency-Key",c.idempotencyKey()).header("X-Cpf-Actor-Id",c.actorId()).header("X-Cpf-Data-Scope",c.dataScope()).POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> res=client.send(req,HttpResponse.BodyHandlers.ofString());int s=res.statusCode();
            if(s==409)throw new EduConflictException("consumer version/idempotency conflict");if(s>=400&&s<500)throw new EduValidationException("consumer rejected request status="+s);if(s>=500)throw new IllegalStateException("consumer retryable failure status="+s);if(s<200||s>=300)throw new IllegalStateException("unexpected consumer status="+s);
            Map<String,Object> data=new LinkedHashMap<>();data.put("status",s);data.put("consumer",uri.toString());data.put("responseDigest",Integer.toHexString(Objects.hashCode(res.body())));
            if(s==202)return EduBusinessConsumerResult.pending("HTTP_202_UNKNOWN_RESULT",Map.copyOf(data));
            return EduBusinessConsumerResult.completed("HTTP_"+s,Map.copyOf(data));
        }catch(EduValidationException|EduConflictException e){throw e;}catch(java.net.http.HttpTimeoutException e){throw new IllegalStateException("consumer timeout",e);}catch(Exception e){throw new IllegalStateException("HTTP consumer failed: "+e.getMessage(),e);}
    }
}
