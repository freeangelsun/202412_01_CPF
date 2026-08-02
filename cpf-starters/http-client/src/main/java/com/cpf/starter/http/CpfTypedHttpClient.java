package com.cpf.starter.http;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
public final class CpfTypedHttpClient {
 private final HttpClient client; private final CpfHttpClientProperties properties;
 public CpfTypedHttpClient(HttpClient client,CpfHttpClientProperties properties){this.client=client;this.properties=properties;}
 public Result execute(String method,URI uri,byte[] body,String contentType,String transactionId,String idempotencyKey,Duration deadline){
  Objects.requireNonNull(uri); if(transactionId==null||transactionId.isBlank())throw new IllegalArgumentException("transactionId is required");
  Duration timeout=deadline==null?properties.getRequestTimeout():deadline.compareTo(properties.getRequestTimeout())<0?deadline:properties.getRequestTimeout();
  var b=HttpRequest.newBuilder(uri).timeout(timeout).header("X-CPF-Transaction-Id",transactionId).header("Content-Type",contentType==null?"application/octet-stream":contentType);
  if(idempotencyKey!=null&&!idempotencyKey.isBlank())b.header("Idempotency-Key",idempotencyKey);
  HttpRequest request=b.method(method,HttpRequest.BodyPublishers.ofByteArray(body==null?new byte[0]:body)).build();
  try{var r=client.send(request,HttpResponse.BodyHandlers.ofByteArray());if(r.body().length>properties.getMaxResponseBytes())throw new IllegalStateException("HTTP response exceeds configured limit");return new Result(r.statusCode(),r.headers().map(),r.body(),false);}
  catch(java.net.http.HttpTimeoutException ex){throw new CpfUnknownHttpResultException("HTTP result is unknown after timeout",ex);}
  catch(InterruptedException ex){Thread.currentThread().interrupt();throw new CpfUnknownHttpResultException("HTTP call interrupted after dispatch",ex);}
  catch(IOException ex){throw new IllegalStateException("HTTP transport failed",ex);}
 }
 public record Result(int status,java.util.Map<String,java.util.List<String>> headers,byte[] body,boolean replayed){public Result{body=body.clone();}public byte[] body(){return body.clone();}}
 public static final class CpfUnknownHttpResultException extends RuntimeException{public CpfUnknownHttpResultException(String m,Throwable c){super(m,c);}}
}
