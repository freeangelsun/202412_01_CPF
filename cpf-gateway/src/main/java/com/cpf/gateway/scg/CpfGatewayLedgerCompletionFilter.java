package com.cpf.gateway.scg;

import com.cpf.core.api.gateway.CpfGatewayAuditEvent;
import com.cpf.core.api.gateway.CpfGatewayLedgerPort;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Sync/Async/Streaming 실제 종료 시 Transaction을 단 한 번 종결하고 Ledger 실패는 recovery spool로 격리합니다. */
@Component
public final class CpfGatewayLedgerCompletionFilter extends OncePerRequestFilter {
    private final CpfGatewayLedgerRecoverySpool recovery;
    private final CpfGatewayAuditRecoverySpool auditRecovery;
    private final long responseBodyBytesCap;

    public CpfGatewayLedgerCompletionFilter(
            CpfGatewayLedgerRecoverySpool recovery,
            CpfGatewayAuditRecoverySpool auditRecovery,
            CpfGatewaySafetyProperties safety) {
        this.recovery = recovery;
        this.auditRecovery = auditRecovery;
        this.responseBodyBytesCap = safety.getResponseBodyBytesCap();
    }

    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
        AtomicLong bytes=new AtomicLong();AtomicBoolean completed=new AtomicBoolean();Throwable[] failure=new Throwable[1];
        HttpServletResponseWrapper counting=counting(response,bytes,responseBodyBytesCap);
        try{
            chain.doFilter(request,counting);
            if(request.isAsyncStarted()){
                request.getAsyncContext().addListener(new AsyncListener(){
                    @Override public void onComplete(AsyncEvent event){finish(request,response,bytes,completed,failure[0],false);}
                    @Override public void onTimeout(AsyncEvent event){finish(request,response,bytes,completed,event.getThrowable(),true);}
                    @Override public void onError(AsyncEvent event){finish(request,response,bytes,completed,event.getThrowable(),true);}
                    @Override public void onStartAsync(AsyncEvent event){try{event.getAsyncContext().addListener(this);}catch(IllegalStateException ignored){}}
                });
            }
        }catch(IOException|ServletException|RuntimeException ex){failure[0]=ex;throw ex;}
        finally{if(!request.isAsyncStarted())finish(request,response,bytes,completed,failure[0],failure[0]!=null);}
    }

    private void finish(HttpServletRequest request,HttpServletResponse response,AtomicLong bytes,AtomicBoolean completed,Throwable failure,boolean unknown){
        if(!completed.compareAndSet(false,true))return;
        Object value=request.getAttribute(CpfScgPrimaryHandler.TX_ATTR);Object startedValue=request.getAttribute(CpfScgPrimaryHandler.START_ATTR);
        if(value instanceof String tx&&startedValue instanceof OffsetDateTime started){
            int status=response.getStatus();boolean unresolved=unknown||failure!=null;
            String result=unresolved?"UNKNOWN_RESULT":status>=500?"FAILED":status>=400?"REJECTED":"SUCCESS";
            String stage=unresolved?"CLIENT_OR_STREAM":status>=500?"UPSTREAM_RESPONSE":"";
            recovery.complete(new CpfGatewayLedgerPort.TransactionCompletion(tx,
                    String.valueOf(request.getAttribute(CpfScgPrimaryHandler.TARGET_ATTR)),result,Integer.toString(status),"",stage,unresolved,
                    Duration.between(started,OffsetDateTime.now()).toMillis(),bytes.get(),OffsetDateTime.now()));
            java.util.Map<String, String> attributes = new java.util.LinkedHashMap<>();
            attributes.put("routeId", String.valueOf(request.getAttribute(CpfScgPrimaryHandler.ROUTE_ATTR)));
            attributes.put("responseBytes", Long.toString(bytes.get()));
            attributes.put("unknownResult", Boolean.toString(unresolved));
            if (!stage.isBlank()) {
                attributes.put("failureStage", stage);
            }
            auditRecovery.record(new CpfGatewayAuditEvent(
                    tx,
                    String.valueOf(request.getAttribute(CpfScgPrimaryHandler.EXECUTION_ATTR)),
                    String.valueOf(request.getAttribute(CpfScgPrimaryHandler.PRINCIPAL_ATTR)),
                    nullable(request.getAttribute(CpfScgPrimaryHandler.REASON_ATTR)),
                    "AFTER",
                    result,
                    nullable(request.getAttribute(CpfScgPrimaryHandler.TARGET_ATTR)),
                    status,
                    java.time.Instant.now(),
                    attributes));
        }
    }

    private static String nullable(Object value){return value==null?null:String.valueOf(value);}

    private static void requireResponseBudget(AtomicLong bytes, long cap, long increment) throws IOException {
        long total = bytes.addAndGet(increment);
        if (total > cap) {
            bytes.addAndGet(-increment);
            throw new IOException("Gateway response body exceeds configured cap");
        }
    }

    private static HttpServletResponseWrapper counting(
            HttpServletResponse response, AtomicLong bytes, long cap) {
        return new HttpServletResponseWrapper(response){
            private ServletOutputStream output;private PrintWriter writer;
            @Override public ServletOutputStream getOutputStream()throws IOException{
                if(writer!=null)throw new IllegalStateException("getWriter already called");
                if(output==null){ServletOutputStream delegate=super.getOutputStream();output=new ServletOutputStream(){
                    @Override public void write(int value)throws IOException{
                        requireResponseBudget(bytes, cap, 1);
                        delegate.write(value);
                    }
                    @Override public void write(byte[] value,int offset,int length)throws IOException{
                        requireResponseBudget(bytes, cap, length);
                        delegate.write(value,offset,length);
                    }
                    @Override public boolean isReady(){return delegate.isReady();}
                    @Override public void setWriteListener(WriteListener listener){delegate.setWriteListener(listener);}
                    @Override public void flush()throws IOException{delegate.flush();}
                    @Override public void close()throws IOException{delegate.close();}
                };}return output;
            }
            @Override public PrintWriter getWriter()throws IOException{
                if(output!=null)throw new IllegalStateException("getOutputStream already called");
                if(writer==null){PrintWriter delegate=super.getWriter();Writer countingWriter=new Writer(){
                    @Override public void write(char[] cbuf,int off,int len)throws IOException{
                        Charset charset = response.getCharacterEncoding() == null
                                ? StandardCharsets.UTF_8
                                : Charset.forName(response.getCharacterEncoding());
                        int encoded = new String(cbuf,off,len).getBytes(charset).length;
                        requireResponseBudget(bytes, cap, encoded);
                        delegate.write(cbuf,off,len);
                    }
                    @Override public void flush(){delegate.flush();}
                    @Override public void close(){delegate.close();}
                };writer=new PrintWriter(countingWriter);}return writer;
            }
        };
    }
}
