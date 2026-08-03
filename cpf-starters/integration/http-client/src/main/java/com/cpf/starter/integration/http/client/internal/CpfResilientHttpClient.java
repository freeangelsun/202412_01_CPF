package com.cpf.starter.integration.http.client.internal;

import com.cpf.core.api.resilience.CpfResilienceCallContext;
import com.cpf.core.api.resilience.CpfResilienceExecutor;
import com.cpf.core.api.resilience.CpfResilienceOutcome;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** Actual HTTP consumer of the provider-neutral CPF resilience boundary. */
public final class CpfResilientHttpClient {
    private final HttpClient client; private final CpfResilienceExecutor resilience;
    public CpfResilientHttpClient(HttpClient client,CpfResilienceExecutor resilience){this.client=Objects.requireNonNull(client);this.resilience=Objects.requireNonNull(resilience);}
    public CpfResilienceOutcome<Response> exchange(String operationId,String transactionId,String idempotencyKey,URI uri,String method,byte[] body,Map<String,String> headers){
        var context=new CpfResilienceCallContext(operationId,transactionId,idempotencyKey,Instant.now(),Map.of("transport","HTTP","host",uri.getHost()==null?"unknown":uri.getHost()));
        return resilience.execute(context,()->{
            try {
                var b=HttpRequest.newBuilder(uri); headers.forEach(b::header);
                HttpRequest request=b.method(method,HttpRequest.BodyPublishers.ofByteArray(body==null?new byte[0]:body)).build();
                var response=client.send(request,HttpResponse.BodyHandlers.ofByteArray());
                return new Response(response.statusCode(),response.headers().map(),response.body());
            } catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("HTTP interrupted",e);}
              catch(IOException e){throw new CpfHttpTransportException("HTTP transport failed",e);}
        });
    }
    public record Response(int statusCode,Map<String,java.util.List<String>> headers,byte[] body){public Response{headers=Map.copyOf(headers);body=body==null?new byte[0]:body.clone();}public byte[] body(){return body.clone();}}
    public static final class CpfHttpTransportException extends RuntimeException{private static final long serialVersionUID=1L;public CpfHttpTransportException(String m,Throwable c){super(m,c);}}
}
