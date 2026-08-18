#!/usr/bin/env python3
from pathlib import Path
import shutil,subprocess,tempfile,textwrap
ROOT=Path(__file__).resolve().parents[2]
def fs(*rels):
 out=[]
 for r in rels:
  p=ROOT/r
  if p.is_dir(): out += [str(x) for x in sorted(p.rglob('*.java'))]
  elif p.is_file(): out.append(str(p))
 return out

def main():
 tmp=Path(tempfile.mkdtemp(prefix='cpf-int-context-'))
 try:
  # Minimal Spring annotation stub only for compiling the real properties contract.
  ann=tmp/'org/springframework/boot/context/properties/ConfigurationProperties.java';ann.parent.mkdir(parents=True)
  ann.write_text('package org.springframework.boot.context.properties; public @interface ConfigurationProperties { String value(); }',encoding='utf-8')
  h=tmp/'com/cpf/integration/http/CpfIntegrationContextHarness.java';h.parent.mkdir(parents=True)
  h.write_text(textwrap.dedent('''
  package com.cpf.integration.http;
  import com.cpf.core.api.context.*;
  import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
  import com.cpf.integration.context.*;
    import java.io.*; import java.net.*; import java.net.http.*; import java.time.*; import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*; import javax.net.ssl.*;
  public final class CpfIntegrationContextHarness {
    static void ok(boolean v,String m){if(!v)throw new IllegalStateException(m);}
    enum Mode { TIMEOUT, CONNECT }
    static final class FakeClient extends HttpClient {
      final Mode mode; final AtomicReference<CpfIntegrationContext> seen=new AtomicReference<>(); FakeClient(Mode m){mode=m;}
      public Optional<java.net.CookieHandler> cookieHandler(){return Optional.empty();}
      public Optional<Duration> connectTimeout(){return Optional.of(Duration.ofSeconds(1));}
      public Redirect followRedirects(){return Redirect.NEVER;}
      public Optional<ProxySelector> proxy(){return Optional.empty();}
      public SSLContext sslContext(){try{return SSLContext.getDefault();}catch(Exception e){throw new RuntimeException(e);}}
      public SSLParameters sslParameters(){return new SSLParameters();}
      public Optional<Authenticator> authenticator(){return Optional.empty();}
      public Version version(){return Version.HTTP_1_1;}
      public Optional<java.util.concurrent.Executor> executor(){return Optional.empty();}
      public <T> HttpResponse<T> send(HttpRequest r,HttpResponse.BodyHandler<T> h)throws IOException,InterruptedException{
        seen.set(CpfIntegrationContexts.requireCurrent());
        if(mode==Mode.CONNECT) throw new ConnectException("refused");
        throw new HttpTimeoutException("timeout after dispatch");
      }
      public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest r,HttpResponse.BodyHandler<T> h){throw new UnsupportedOperationException();}
      public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest r,HttpResponse.BodyHandler<T> h,HttpResponse.PushPromiseHandler<T> p){throw new UnsupportedOperationException();}
    }
    static CpfContext root(){Instant n=Instant.now();return new CpfContext(
      new CpfContext.CpfTransactionContext("20260809000000000EDUlocal010000002","20260809000000000EDUlocal010000002",null,"CORR-I","TRACE-I","WEB","EDU","WEB","EDU",LocalDate.of(2026,8,9),n,CpfContext.CpfTransactionOriginKind.HTTP,"EDU",null),
      new CpfContext.CpfExecutionContext("OEDU030001","EX-I-ROOT","EX-I-ROOT",null,"SG-I-ROOT",null,CpfContext.CpfExecutionType.API,1,0,n,n.plusSeconds(60),CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
      new CpfContext.CpfOperationContext("OP-I","pay","CMD-I","IDEM-I",CpfContext.CpfIdempotencyScope.TRANSACTION,CpfContext.CpfIdempotencyMode.REQUIRED,null,null),null,null);}
    public static void main(String[] a)throws Exception{
      AtomicInteger n=new AtomicInteger(); CpfExecutionIdGenerator ids=new CpfExecutionIdGenerator(){public String newExecutionId(){return "EX-I-"+n.incrementAndGet();}public String newSegmentId(){return "SG-I-"+n.incrementAndGet();}};
      CpfHttpClientProperties p=new CpfHttpClientProperties(); p.setAllowedHosts(Set.of("localhost"));p.validate();
      try(AutoCloseable rootScope=CpfContexts.bind(CpfContextSnapshot.capture(root()))){
        FakeClient timeout=new FakeClient(Mode.TIMEOUT); CpfTypedHttpClient c=new CpfTypedHttpClient(timeout,p,ids);
        String unknown=null; try{c.execute("POST",URI.create("http://localhost/pay"),new byte[]{1},"application/octet-stream",Duration.ofSeconds(2));}
        catch(CpfTypedHttpClient.CpfUnknownHttpResultException e){unknown=e.unknownOutcomeId();}
        ok(unknown!=null&&unknown.contains("UNKNOWN"),"unknown id missing");
        ok(timeout.seen.get()!=null,"integration context not bound to transport");
        ok("localhost".equals(timeout.seen.get().partnerSystemCode()),"partner context");
        ok(timeout.seen.get().logicalEndpointId().contains("POST localhost/pay"),"endpoint context");
        ok("IDEM-I".equals(timeout.seen.get().idempotencyKey()),"idempotency context");
        ok(CpfIntegrationContexts.current()==null,"integration context leak after unknown");
        ok("EX-I-ROOT".equals(CpfContexts.requireCurrent().executionId()),"core parent restore after unknown");

        FakeClient connect=new FakeClient(Mode.CONNECT); CpfTypedHttpClient pdc=new CpfTypedHttpClient(connect,p,ids);boolean pre=false;
        try{pdc.execute("POST",URI.create("http://localhost/pay"),new byte[]{1},"application/octet-stream",Duration.ofSeconds(2));}
        catch(CpfTypedHttpClient.CpfHttpPreDispatchException e){pre=true;}
        ok(pre,"pre-dispatch classification"); ok(CpfIntegrationContexts.current()==null,"integration context leak after pre-dispatch");
        boolean denied=false;try{c.execute("GET",URI.create("http://evil.example/test"),null,null,Duration.ofSeconds(1));}catch(SecurityException e){denied=true;}ok(denied,"host allowlist");
      }
      ok(CpfContexts.current()==null,"core context leak");
    }
  }
  '''),encoding='utf-8')
  src=fs('cpf-core/src/main/java/com/cpf/core/api/context/CpfContext.java','cpf-core/src/main/java/com/cpf/core/api/context/CpfContextSnapshot.java','cpf-core/src/main/java/com/cpf/core/api/context/CpfContexts.java','cpf-core/src/main/java/com/cpf/core/spi/context/CpfContextRuntimeProvider.java','cpf-starters/base/runtime/src/main/java/com/cpf/starter/internal/context/CpfStarterContextRuntime.java','cpf-starters/base/runtime/src/main/java/com/cpf/foundation/id/spi/CpfExecutionIdGenerator.java','cpf-starters/integration/src/main/java/com/cpf/integration/context','cpf-starters/integration/src/main/java/com/cpf/integration/internal/context/CpfIntegrationContextRuntime.java','cpf-starters/integration/http/src/main/java/com/cpf/integration/http/CpfHttpClientProperties.java','cpf-starters/integration/http/src/main/java/com/cpf/integration/http/CpfTypedHttpClient.java')+[str(ann),str(h)]
  out=tmp/'classes';out.mkdir();cp=subprocess.run(['javac','-encoding','UTF-8','-d',str(out),*src],text=True,capture_output=True)
  if cp.returncode: print('CPF_INTEGRATION_CONTEXT_RUNTIME=FAIL compile');print(cp.stdout);print(cp.stderr);return cp.returncode
  service=out/'META-INF/services/com.cpf.core.spi.context.CpfContextRuntimeProvider';service.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(ROOT/'cpf-starters/base/runtime/src/main/resources/META-INF/services/com.cpf.core.spi.context.CpfContextRuntimeProvider',service)
  r=subprocess.run(['java','-cp',str(out),'com.cpf.integration.http.CpfIntegrationContextHarness'],text=True,capture_output=True)
  if r.returncode: print('CPF_INTEGRATION_CONTEXT_RUNTIME=FAIL runtime');print(r.stdout);print(r.stderr);return r.returncode
  print(f'CPF_INTEGRATION_CONTEXT_RUNTIME=PASS sources={len(src)}');return 0
 finally:shutil.rmtree(tmp,ignore_errors=True)
if __name__=='__main__':raise SystemExit(main())
