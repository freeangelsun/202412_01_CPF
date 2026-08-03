package com.cpf.starter.integration.tcp.internal;

import com.cpf.core.api.resilience.*;
import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** Actual TCP consumer with UNKNOWN_RESULT delegated to the CPF resilience policy. */
public final class CpfResilientTcpClient {
    private final CpfResilienceExecutor resilience;
    public CpfResilientTcpClient(CpfResilienceExecutor resilience){this.resilience=Objects.requireNonNull(resilience);}
    public CpfResilienceOutcome<byte[]> exchange(String operationId,String transactionId,String idempotencyKey,String host,int port,byte[] request,int maxResponseBytes,int connectTimeoutMillis,int readTimeoutMillis){
        if(maxResponseBytes<1||maxResponseBytes>16*1024*1024)throw new IllegalArgumentException("invalid maxResponseBytes");
        var context=new CpfResilienceCallContext(operationId,transactionId,idempotencyKey,Instant.now(),Map.of("transport","TCP","host",host,"port",Integer.toString(port)));
        return resilience.execute(context,()->{
            try(var socket=new Socket()){
                socket.connect(new InetSocketAddress(host,port),connectTimeoutMillis);socket.setSoTimeout(readTimeoutMillis);
                socket.getOutputStream().write(request);socket.getOutputStream().flush();
                var out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
                while((n=socket.getInputStream().read(buffer))>=0){if(out.size()+n>maxResponseBytes)throw new IllegalStateException("TCP response exceeds limit");out.write(buffer,0,n);if(n==0)break;}
                return out.toByteArray();
            }catch(IOException e){throw new CpfTcpTransportException("TCP transport failed",e);}
        });
    }
    public static final class CpfTcpTransportException extends RuntimeException{private static final long serialVersionUID=1L;public CpfTcpTransportException(String m,Throwable c){super(m,c);}}
}
