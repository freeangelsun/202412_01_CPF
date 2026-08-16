package com.cpf.platform.operations.observability.otlp;
import com.cpf.platform.operations.observability.api.CpfTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Scope;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
final class CpfOtlpTelemetryAdapter implements CpfTelemetry {
 private final Tracer tracer; private final String endpoint; private final AtomicLong started=new AtomicLong(),ended=new AtomicLong(),failures=new AtomicLong();
 CpfOtlpTelemetryAdapter(Tracer tracer,String endpoint){this.tracer=tracer;this.endpoint=endpoint;}
 public CpfTelemetrySpan startSpan(String name,String kind,Map<String,String> attributes){try{Span span=tracer.spanBuilder(name).setSpanKind(toKind(kind)).startSpan();if(attributes!=null)attributes.forEach((k,v)->{if(k!=null&&v!=null&&!sensitive(k))span.setAttribute(k,v);});Scope scope=span.makeCurrent();started.incrementAndGet();return new CpfTelemetrySpan(){private boolean closed;public void error(Throwable t){if(t!=null){span.recordException(t);span.setStatus(StatusCode.ERROR);}}public void close(){if(closed)return;closed=true;try{scope.close();span.end();ended.incrementAndGet();}catch(RuntimeException e){failures.incrementAndGet();}}};}catch(RuntimeException e){failures.incrementAndGet();return new CpfTelemetrySpan(){public void error(Throwable t){}public void close(){}};}}
 public Map<String,Object> status(){Map<String,Object> m=new LinkedHashMap<>();m.put("enabled",true);m.put("provider","OPENTELEMETRY_OTLP");m.put("endpoint",endpoint);m.put("startedSpanCount",started.get());m.put("endedSpanCount",ended.get());m.put("instrumentationFailureCount",failures.get());return m;}
 private static SpanKind toKind(String k){if(k==null)return SpanKind.INTERNAL;try{return SpanKind.valueOf(k.trim().toUpperCase());}catch(Exception e){return SpanKind.INTERNAL;}}
 private static boolean sensitive(String k){String n=k.toLowerCase().replace("-","").replace("_","");return n.contains("password")||n.contains("secret")||n.contains("token")||n.contains("authorization")||n.contains("cookie")||n.contains("account")||n.contains("memberno")||n.contains("customerno");}
}
